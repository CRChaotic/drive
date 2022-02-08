package controller;

import exception.InvalidUserInfoException;
import exception.SameEmailException;
import exception.SameUsernameException;
import exception.UserNotExistException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pojo.User;
import service.UserService;
import utils.Identifier;
import utils.MailSender;

import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@RestController
@RequestMapping( "/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterForm registerForm, HttpSession session) {
        User user = registerForm.getUser();
        user.setCapacity(10240L);
        String verificationCode = registerForm.getVerificationCode();

        if (!validUserInfo(user)) {
            return new ResponseEntity<>("Invalid user info", HttpStatus.OK);
        }
        LocalDateTime lastTimeSendingVerification = (LocalDateTime) session.getAttribute("lastTime");
        String vCode = (String) session.getAttribute("verificationCode");
        if (lastTimeSendingVerification == null || !verificationCode.equals(vCode)) {
            return new ResponseEntity<>("Verification code is wrong", HttpStatus.OK);
        }
        //Verification code has 5 minutes expiry time
        if (lastTimeSendingVerification.plusMinutes(5L).isBefore(LocalDateTime.now())) {
            return new ResponseEntity<>("Expired verification code", HttpStatus.OK);
        }
        try {
            Identifier identifier = new Identifier("SHA3-256");
            identifier.read(user.getPassword().getBytes(StandardCharsets.UTF_8));
            user.setPassword(identifier.getUniqueId());
            userService.saveUser(user);
            return new ResponseEntity<>("Registering successfully", HttpStatus.CREATED);
        } catch (InvalidUserInfoException invalidUserInfoException) {
            return new ResponseEntity<>("Invalid user info message", HttpStatus.OK);
        } catch (SameUsernameException sameUsernameException) {
            return new ResponseEntity<>("Username had been used", HttpStatus.OK);
        } catch (SameEmailException sameEmailException) {
            return new ResponseEntity<>("Email had been used", HttpStatus.OK);
        }
    }

    @PostMapping(value = "/login")
    public ResponseEntity<String> login(@RequestBody User user, HttpSession session) {
        try {
            Identifier identifier = new Identifier("SHA3-256");
            identifier.read(user.getPassword().getBytes(StandardCharsets.UTF_8));
            user.setPassword(identifier.getUniqueId());
            User u = userService.authorizeUser(user);
            if (u != null) {
                session.setAttribute("user", u);
                return new ResponseEntity<>("Logging successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Logging failed", HttpStatus.OK);
            }
        } catch (UserNotExistException userNotExistException) {
            return new ResponseEntity<>("User not exists", HttpStatus.NOT_FOUND);
        }
    }

    @PatchMapping(value = "/password")
    public ResponseEntity<String> modifyUserPassword(@RequestBody ModifyPasswordForm passwordForm, HttpSession session) {
        String password = passwordForm.getPassword();
        User user = (User) session.getAttribute("user");
        String oldPassword = user.getPassword();
        //check out whether the old password is right
        if (oldPassword.equals(passwordForm.getOldPassword())) {
            //check out invalidation of new password
            if (password != null && password.length() > 5 && password.length() < 17 && !password.contains(" ")) {
                Identifier identifier = new Identifier("SHA3-256");
                identifier.read(password.getBytes(StandardCharsets.UTF_8));
                userService.modifyUserPasswordByUsername(user, identifier.getUniqueId());
                user.setPassword(identifier.getUniqueId());
                session.setAttribute("user", user);
                return new ResponseEntity<>("Modified password successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Invalid password,length of password should be from 6 to 16 and not containing blank character", HttpStatus.OK);
            }
        }
        return new ResponseEntity<>("Original password is wrong", HttpStatus.OK);
    }

    @PatchMapping("/email")
    public ResponseEntity<String> modifyUserEmail(@RequestBody ModifyEmailForm emailForm, HttpSession session) {
        String verificationCode = emailForm.getVerificationCode();
        String email = emailForm.getEmail();
        User user = (User) session.getAttribute("user");
        String vCode = (String) session.getAttribute("verificationCode");
        if (verificationCode.equals(vCode)) {
            if (email != null && email.length() > 4 && email.length() < 65 && !email.contains(" ")) {
                userService.modifyUserEmailByUsername(user, email);
                session.removeAttribute("verificationCode");
                return new ResponseEntity<>("Modified email successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Invalid email", HttpStatus.OK);
            }
        }
        return new ResponseEntity<>("Verification code is wrong", HttpStatus.OK);

    }

    @GetMapping("/verification")
    public ResponseEntity<String> getVerificationCode(@RequestParam String email, HttpSession session) {
        LocalDateTime dateTime = (LocalDateTime) session.getAttribute("lastTime");
        if (dateTime == null || dateTime.plusMinutes(1).isBefore(LocalDateTime.now())) {
            String randomNumbers = MailSender.sendMailTo(email);
            System.out.println("verification code:" + randomNumbers);
            session.setAttribute("verificationCode", randomNumbers);
            session.setAttribute("lastTime", LocalDateTime.now());
            return new ResponseEntity<>("Sending verification code successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Sending verification code is not enough interval time from last time", HttpStatus.BAD_REQUEST);
        }
    }

    public boolean validUserInfo(User user) {
        int passed = 0;
        if (user.getUsername() != null && user.getUsername().length() > 0 && user.getUsername().length() < 17 && !user.getUsername().contains(" ")) {
            passed++;
        }
        if (user.getPassword() != null && user.getPassword().length() > 5 && user.getPassword().length() < 17 && !user.getPassword().contains(" ")) {
            passed++;
        }
        if (user.getEmail() != null && user.getEmail().length() > 3 && user.getEmail().length() < 65 && !user.getEmail().contains(" ")) {
            passed++;
        }
        return passed == 3;
    }
}

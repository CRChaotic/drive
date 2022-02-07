package service.implement;

import dao.UserDao;
import exception.*;
import org.springframework.stereotype.Service;
import pojo.Role;
import pojo.User;
import pojo.UserStatus;
import service.UserService;

import java.sql.Timestamp;
import java.time.Instant;

@Service("userService")
public class UserServiceImpl implements UserService {
    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public void saveUser(User user) {
        if(!validateUserInfo(user)){
            throw new InvalidUserInfoException();
        }
        User u = userDao.findUserByUsername(user.getUsername());
        if(u != null){
            throw new SameUsernameException();
        }
        u = new User();
        u.setEmail(user.getEmail());
        if(userDao.findUsers(u).size() != 0){
            throw new SameEmailException();
        }
        user.setCreatedTime(Timestamp.from(Instant.now()));
        if(user.getRole() == null){
            user.setRole(Role.NORMAL);
        }
        if(user.getUserStatus() == null){
            user.setUserStatus(UserStatus.NORMAL);
        }
        userDao.addUser(user);
    }

    @Override
    public User authorizeUser(User user) {
        User u = userDao.findUserByUsername(user.getUsername());
        if(u == null){
            throw new UserNotExistException();
        }
        if(u.getPassword().equals(user.getPassword()) && u.getUserStatus() == UserStatus.NORMAL){
            return u;
        }else{
            return null;
        }
    }

    @Override
    public void modifyUserPasswordByUsername(User user, String password) {
        User u = authorizeUser(user);
        if(u == null){
            throw new UnauthorizedUserException();
        }
        u.setPassword(password);
        if(!validateUserInfo(u)){
            throw new InvalidUserInfoException();
        }
        userDao.updateUserByUsername(u.getUsername(),u);
    }

    @Override
    public void modifyUserEmailByUsername(User user, String email) {
        User u = authorizeUser(user);
        if(u == null){
            throw new UnauthorizedUserException();
        }
        u.setEmail(email);
        if(!validateUserInfo(u)){
            throw new InvalidUserInfoException();
        }
        userDao.updateUserByUsername(u.getUsername(),u);
    }

    private boolean validateUserInfo(User user){
        if(user.getUsername().length() < 1 || user.getUsername().length() > 16){
            return false;
        }
        if(user.getPassword().length() < 1 || user.getPassword().length() > 64){
            return false;
        }
        if(user.getEmail().length() < 1 || user.getEmail().length() > 64){
            return false;
        }
        if(user.getCapacity() < 0){
            return false;
        }
        return true;
    }
}

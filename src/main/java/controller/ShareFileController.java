package controller;

import exception.AccessTokenErrorException;
import exception.ExpiredTimeException;
import exception.UserFileOwnerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pojo.ShareInfo;
import pojo.User;
import pojo.UserFile;
import service.ShareFileService;

import javax.servlet.http.HttpSession;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/share")
public class ShareFileController {
    private final ShareFileService shareFileService;

    public ShareFileController(ShareFileService shareFileService) {
        this.shareFileService = shareFileService;
    }

    @PostMapping("/save")
    public ResponseEntity<String> saveShareInfo(@RequestBody ShareFileInfoForm shareFileInfoForm, HttpSession session) {
        User user = (User) session.getAttribute("user");
        List<UserFile> userFiles = new ArrayList<>();
        shareFileInfoForm.getUserFileIds().forEach(id -> {
            UserFile userFile = new UserFile();
            userFile.setId(id);
            userFiles.add(userFile);
        });
        ShareInfo shareInfo = new ShareInfo();
        shareInfo.setUsername(user.getUsername());
        shareInfo.setUserFiles(userFiles);
        shareInfo.setCreatedTime(Timestamp.from(Instant.now()));
        if (shareFileInfoForm.getAccessToken() != null)
            shareInfo.setAccessToken(shareFileInfoForm.getAccessToken());
        if (shareFileInfoForm.getExpiryTime() != null)
            try {
                shareInfo.setExpiryTime(Timestamp.valueOf(shareFileInfoForm.getExpiryTime()));
            } catch (IllegalArgumentException illegalArgumentException) {
                return new ResponseEntity<>("ExpiryTime should be in the format of yyyy-MM-dd HH:mm:ss", HttpStatus.BAD_REQUEST);
            }
        try {
            shareFileService.saveShareInfo(user, shareInfo);
            return new ResponseEntity<>(shareInfo.getId(), HttpStatus.OK);
        } catch (UserFileOwnerException userFileOwnerException) {
            return new ResponseEntity<>("User does not own some of user files", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/all")
    public List<ShareInfo> getAllShareInfo(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return shareFileService.getAllShareInfo(user);
    }

    @GetMapping("/get/{shareInfoId}")
    public ResponseEntity<?> getShareInfoByIdAndAccessToken(@PathVariable String shareInfoId, @RequestParam String accessToken, @RequestParam int directoryId) {
        try {
            ShareInfo shareInfo = shareFileService.getShareInfoByIdAndAccessToken(shareInfoId, accessToken, directoryId);
            if(shareInfo == null)
                return new ResponseEntity<>("Share info does not exist",HttpStatus.BAD_REQUEST);
            else
                return new ResponseEntity<>(shareInfo,HttpStatus.OK);
        } catch (AccessTokenErrorException | ExpiredTimeException accessTokenErrorException) {
            return new ResponseEntity<>("Either accessToken is wrong or expiryTime expired",HttpStatus.BAD_REQUEST);
        }
    }

    @PatchMapping("/accessToken")
    public ResponseEntity<String> modifyShareInfoAccessTokenById(@RequestBody ModifyAccessTokenForm accessTokenForm, HttpSession session) {
        User user = (User) session.getAttribute("user");
        shareFileService.modifyShareInfoAccessTokenById(user, accessTokenForm.getShareInfoId(), accessTokenForm.getAccessToken());
        return new ResponseEntity<>("Modified " + accessTokenForm.getShareInfoId() + " successfully", HttpStatus.OK);

    }

    @PatchMapping("/expiryTime")
    public ResponseEntity<String> modifyShareInfoExpiryTimeById(@RequestBody ModifyExpiryTimeForm expiryTimeForm, HttpSession session) {
        User user = (User) session.getAttribute("user");
        Timestamp timestamp;
        try {
            timestamp = Timestamp.valueOf(expiryTimeForm.getExpiryTime());
        }catch (IllegalArgumentException illegalArgumentException){
            return new ResponseEntity<>("Modified " + expiryTimeForm.getShareInfoId() + " failed,expiryTime should be in the format of yyyy-MM-dd HH:mm:ss", HttpStatus.BAD_REQUEST);
        }
        shareFileService.modifyShareInfoExpiryTimeById(user, expiryTimeForm.getShareInfoId(), timestamp);
        return new ResponseEntity<>("Modified " + expiryTimeForm.getShareInfoId() + " successfully", HttpStatus.OK);
    }

    @DeleteMapping("/remove/{shareInfoId}")
    public void removeShareInfoById(@PathVariable String shareInfoId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        shareFileService.removeShareInfoById(user, shareInfoId);
    }
}

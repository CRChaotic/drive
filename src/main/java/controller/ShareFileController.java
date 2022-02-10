package controller;

import exception.AccessTokenErrorException;
import exception.ExpiredTimeException;
import exception.UserFileOwnerException;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pojo.ShareInfo;
import pojo.User;
import pojo.UserFile;
import service.ShareFileService;
import utils.FileTypeConverter;
import utils.Zipper;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/share")
public class ShareFileController {
    private final ShareFileService shareFileService;
    private final File fileStorageDir = new File("../fileStorage");
    private final File tempDir = new File("../temp");

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

    @GetMapping("/download/{shareInfoId}")
    public ResponseEntity<byte[]> downloadSharedUserFileByUserFileIds(@PathVariable String shareInfoId,@RequestParam String accessToken,@RequestParam List<Integer> userFileIds){
        try {
            String filename = null;
            String fileId;
            File file = null;
            if(userFileIds.size() == 1){
                UserFile sharedUserFile = shareFileService.getSharedUserFileByUserFileId(shareInfoId,accessToken,userFileIds.get(0));
                if(sharedUserFile != null && !sharedUserFile.getType().equals(FileTypeConverter.DIRECTORY_TYPE)){
                    filename = sharedUserFile.getFilename();
                    fileId = sharedUserFile.getFileId();
                    file = new File(fileStorageDir+"/"+fileId);
                }else if(sharedUserFile != null){
                    filename = sharedUserFile.getFilename() + ".zip";
                    fileId = UUID.randomUUID().toString();
                    Zipper zipper = new Zipper(tempDir + "/" + fileId);
                    ShareInfo shareInfo = shareFileService.getShareInfoByIdAndAccessToken(shareInfoId, accessToken,sharedUserFile.getId());
                    List<UserFile> userFiles = shareInfo.getUserFiles();
                    userFiles.forEach(
                            uf -> {
                                if (uf.getType().equals(FileTypeConverter.DIRECTORY_TYPE)) {
                                    recursivelyAddZipEntry(shareInfoId,accessToken, uf.getId(), zipper, uf.getFilename());
                                } else {
                                    zipper.addEntry(uf.getFilename(), fileStorageDir + "/" + uf.getFileId());
                                }
                            }
                    );
                    zipper.done();
                    file = new File(tempDir + "/" + fileId);
                }
            }else if(userFileIds.size() > 1){
                fileId = UUID.randomUUID().toString();
                filename = fileId+".zip";
                Zipper zipper = new Zipper(tempDir + "/" + fileId);
                userFileIds.forEach(
                        id -> {
                            UserFile userFile = shareFileService.getSharedUserFileByUserFileId(shareInfoId,accessToken,id);
                            if (userFile != null && userFile.getType().equals(FileTypeConverter.DIRECTORY_TYPE)) {
                                recursivelyAddZipEntry(shareInfoId,accessToken, userFile.getId(),zipper,userFile.getFilename());
                            } else if (userFile != null) {
                                zipper.addEntry(userFile.getFilename(), fileStorageDir + "/" + userFile.getFileId());
                            }
                        }
                );
                zipper.done();
                file = new File(tempDir + "/" + fileId);
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("filename",filename);
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            if(file != null)
                return new ResponseEntity<>(FileUtils.readFileToByteArray(file),headers,HttpStatus.OK);
            else
                return new ResponseEntity<>(null,HttpStatus.BAD_REQUEST);
        } catch (AccessTokenErrorException | ExpiredTimeException exception) {
            return new ResponseEntity<>(null,HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(null,HttpStatus.NOT_FOUND);
        }
    }

    private void recursivelyAddZipEntry(String shareInfoId, String accessToken,int directoryId, Zipper zipper, String directoryName) {
        ShareInfo shareInfo = shareFileService.getShareInfoByIdAndAccessToken(shareInfoId, accessToken,directoryId);
        List<UserFile> userFiles = shareInfo.getUserFiles();
        userFiles.forEach(userFile -> {
            if (userFile.getType().equals(FileTypeConverter.DIRECTORY_TYPE)) {
                recursivelyAddZipEntry(shareInfoId,accessToken, userFile.getId(), zipper, directoryName + "/" + userFile.getFilename());
            } else {
                zipper.addEntry(directoryName + "/" + userFile.getFilename(), fileStorageDir + "/" + userFile.getFileId());
            }
        });
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

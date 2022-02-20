package controller;

import controller.form.CreateDirectoryForm;
import controller.form.ReportUserFileForm;
import exception.EmptyFilenameException;
import exception.FileNotExistException;
import exception.UserFileOwnerException;
import org.apache.commons.io.FileUtils;
import org.springframework.http.*;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pojo.FileStatus;
import pojo.User;
import pojo.UserFile;
import pojo.UserFileStatus;
import service.UserFileService;
import utils.FileTypeConverter;
import utils.Identifier;
import utils.Zipper;

import javax.servlet.http.HttpSession;
import java.io.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/file")
public class UserFileController {
    private final UserFileService userFileService;
    //actual file existing directory
    private final File fileStorageDir = new File("../fileStorage");
    private final File tempDir = new File("../temp");

    public UserFileController(UserFileService userFileService) {
        this.userFileService = userFileService;
        if (!fileStorageDir.exists() && !fileStorageDir.mkdir()) {
            throw new RuntimeException("Create fileStorage directory failed");
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<UserFile> upload(@RequestParam("file") MultipartFile multipartFile, @RequestParam("parentDirectoryId") int parentDirectoryId, HttpSession session) {
        try {
            //create temp file then deleting it when exit regardless if it exists or not
            File tmpFile = File.createTempFile("tmp-", "", fileStorageDir);
            InputStream in = multipartFile.getInputStream();
            OutputStream out = new FileOutputStream(tmpFile);
            byte[] bytes = new byte[1024];
            int length;
            Identifier identifier = new Identifier("SHA3-256");
            while ((length = in.read(bytes)) != -1) {
                out.write(bytes, 0, length);
                identifier.read(bytes);
            }
            in.close();
            out.close();
            String fileId = identifier.getUniqueId();
            //make up user file
            User user = (User) session.getAttribute("user");
            UserFile userFile = new UserFile();
            userFile.setFileId(fileId);
            userFile.setUsername(user.getUsername());
            userFile.setDirectory(parentDirectoryId);
            userFile.setFilename(multipartFile.getOriginalFilename());
            userFile.setType(multipartFile.getContentType());
            userFile.setSize(multipartFile.getSize());
            userFile.setUserFileStatus(UserFileStatus.NORMAL);
            userFile.setFileStatus(FileStatus.NORMAL);
            userFile.setCreatedTime(Timestamp.from(Instant.now()));
            File file = new File(fileStorageDir.getPath() + "/" + fileId);
            //rename temp file if it is not existing
            if (!file.exists() && !tmpFile.renameTo(file)) {
                return new ResponseEntity<>(null, HttpStatus.EXPECTATION_FAILED);
            } else if (!tmpFile.delete()) {
                System.out.println("somehow temp file wasn't delete");
            }
            return new ResponseEntity<>(userFileService.saveUserFile(user, userFile), HttpStatus.CREATED);
        } catch (IOException e) {
            return new ResponseEntity<>(null, HttpStatus.EXPECTATION_FAILED);
        }
    }

    @PostMapping("/directory")
    public ResponseEntity<String> createUserDirectory(@RequestBody CreateDirectoryForm directoryForm, HttpSession session) {
        User user = (User) session.getAttribute("user");
        try {
            if (userFileService.saveUserDirectory(user, directoryForm.getParentDirectoryId(), directoryForm.getDirectoryName())) {
                return new ResponseEntity<>("Created directory " + directoryForm.getDirectoryName(), HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>("Directory exists", HttpStatus.OK);
            }
        } catch (EmptyFilenameException emptyFilenameException) {
            return new ResponseEntity<>("filename cannot be empty" + directoryForm.getDirectoryName(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadUserFiles(@RequestParam List<Integer> userFileIds, HttpSession session) throws IOException {
        User user = (User) session.getAttribute("user");
        String filename = null;
        String fileId;
        File file = null;
        HttpHeaders headers = new HttpHeaders();
        if (userFileIds.size() == 1) {
            UserFile userFile = userFileService.getUserFileById(user, userFileIds.get(0));
            //straight up downloading if it is a normal file
            if (userFile != null && !userFile.getType().equals(FileTypeConverter.DIRECTORY_TYPE)) {
                filename = userFile.getFilename();
                fileId = userFile.getFileId();
                file = new File(fileStorageDir + "/" + fileId);
            } else if (userFile != null) {
                filename = userFile.getFilename() + ".zip";
                fileId = UUID.randomUUID().toString();
                Zipper zipper = new Zipper(tempDir + "/" + fileId);
                List<UserFile> userFiles = userFileService.getUserFilesByDirectoryId(user, userFile.getId());
                userFiles.forEach(
                        uf -> {
                            if (uf.getType().equals(FileTypeConverter.DIRECTORY_TYPE)) {
                                recursivelyAddZipEntry(user, uf.getId(), zipper, uf.getFilename());
                            } else {
                                zipper.addEntry(uf.getFilename(), fileStorageDir + "/" + uf.getFileId());
                            }
                        }
                );
                zipper.done();
                file = new File(tempDir + "/" + fileId);
            }
        }
        //multiple files downloading
        if (userFileIds.size() > 1) {
            fileId = UUID.randomUUID().toString();
            filename = fileId+".zip";
            Zipper zipper = new Zipper(tempDir + "/" + fileId);
            userFileIds.forEach(
                    id -> {
                        UserFile userFile = userFileService.getUserFileById(user, id);
                        if (userFile != null && userFile.getType().equals(FileTypeConverter.DIRECTORY_TYPE)) {
                            recursivelyAddZipEntry(user, userFile.getId(),zipper,userFile.getFilename());
                        } else if (userFile != null) {
                            zipper.addEntry(userFile.getFilename(), fileStorageDir + "/" + userFile.getFileId());
                        }
                    }
            );
            zipper.done();
            file = new File(tempDir + "/" + fileId);
        }

        if(file != null && filename != null){
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
            return new ResponseEntity<>(FileUtils.readFileToByteArray(file), headers, HttpStatus.OK);
        }else{
            return new ResponseEntity<>(null,HttpStatus.BAD_REQUEST);
        }
    }

    private void recursivelyAddZipEntry(User user, int directoryId, Zipper zipper, String directoryName) {
        List<UserFile> userFiles = userFileService.getUserFilesByDirectoryId(user, directoryId);
        userFiles.forEach(userFile -> {
            if (userFile.getType().equals(FileTypeConverter.DIRECTORY_TYPE)) {
                recursivelyAddZipEntry(user, userFile.getId(), zipper, directoryName + "/" + userFile.getFilename());
            } else {
                zipper.addEntry(directoryName + "/" + userFile.getFilename(), fileStorageDir + "/" + userFile.getFileId());
            }
        });
    }

    @GetMapping("/directory/{directoryId}")
    public ResponseEntity<List<UserFile>> getUserFilesByDirectory(@PathVariable int directoryId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        return new ResponseEntity<>(userFileService.getUserFilesByDirectoryId(user, directoryId), HttpStatus.OK);
    }

    @PatchMapping("/directory/{userFileId}/to/{directoryId}")
    public ResponseEntity<String> changeUserFileDirectory(@PathVariable int userFileId, @PathVariable int directoryId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        try {
            userFileService.modifyUserFileDirectory(user, userFileId, directoryId);
            return new ResponseEntity<>("Moved user file " + userFileId + " to directory " + directoryId, HttpStatus.OK);
        } catch (UserFileOwnerException userFileOwnerException) {
            return new ResponseEntity<>("Doesn't have the user file or directory id is not a directory file", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(params = {"type"})
    public ResponseEntity<List<UserFile>> getUserFilesByType(@NonNull String type, HttpSession session) {
        User user = (User) session.getAttribute("user");
        return new ResponseEntity<>(userFileService.getUserFilesByWildcardType(user, type), HttpStatus.OK);
    }

    @GetMapping(params = {"filename"})
    public ResponseEntity<List<UserFile>> getUserFilesByFilename(@NonNull String filename, HttpSession session) {
        User user = (User) session.getAttribute("user");
        return new ResponseEntity<>(userFileService.getUserFilesByWildcardFilename(user, filename), HttpStatus.OK);
    }

    @PatchMapping("/remove/{userFileId}")
    public String removeUserFileTemporarilyById(@PathVariable int userFileId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        UserFile userFile = userFileService.getUserFileById(user, userFileId);
        if (userFile != null && userFile.getType().equals(FileTypeConverter.DIRECTORY_TYPE)) {
            userFileService.removeTemporarilyUserDirectoryById(user, userFileId);
        } else if (userFile != null) {
            userFileService.removeTemporarilyUserFileById(user, userFileId);
        }
        return "User file id of " + userFileId + " was removed to trash bin";
    }

    @GetMapping("/remove")
    public List<UserFile> getRemovedTemporarilyUserFiles(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return userFileService.getRemovedTemporarilyUserFiles(user);
    }

    @PatchMapping("/restore/{userFileId}")
    public String restoreUserFileById(@PathVariable int userFileId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        UserFile userFile = userFileService.getUserFileById(user, userFileId);
        if (userFile != null && userFile.getType().equals(FileTypeConverter.DIRECTORY_TYPE)) {
            userFileService.restoreUserDirectoryById(user, userFileId);
        } else if (userFile != null) {
            userFileService.restoreUserFileById(user, userFileId);
        }
        return "User file id of " + userFileId + " was restored";
    }

    @DeleteMapping("/remove/{userFileId}")
    public ResponseEntity<String> removeUserFilesById(@PathVariable int userFileId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        UserFile userFile = userFileService.getUserFileById(user, userFileId);
        if (userFile != null && userFile.getType().equals(FileTypeConverter.DIRECTORY_TYPE)) {
            userFileService.removeUserDirectoryById(user, userFileId);
        } else if (userFile != null) {
            userFileService.removeUserFileById(user, userFileId);
        }
        return new ResponseEntity<>("User file id of " + userFileId + " was deleted", HttpStatus.OK);
    }

    @PatchMapping("/rename/{userFileId}")
    public String renameUserFileById(@RequestParam String filename, @PathVariable int userFileId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        userFileService.renameUserFileById(user, userFileId, filename);
        return "Renamed file";
    }

    @PostMapping("/report")
    public ResponseEntity<String> reportUserFileByFileId(@RequestBody ReportUserFileForm userFileForm, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (userFileForm.getReason().isEmpty()) {
            return new ResponseEntity<>("Reason cannot be empty", HttpStatus.BAD_REQUEST);
        }
        try {
            userFileService.reportUserFileByFileId(user, userFileForm.getFileId(), userFileForm.getReason());
            return new ResponseEntity<>("Reported successfully", HttpStatus.OK);
        } catch (FileNotExistException fileNotExistException) {
            return new ResponseEntity<>("Reported file doesn't exist", HttpStatus.BAD_REQUEST);
        }
    }
}

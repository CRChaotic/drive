package service.implement;

import dao.FileDao;
import dao.ReportedFileDao;
import dao.UserFileDao;
import exception.*;
import org.springframework.stereotype.Service;
import pojo.*;
import service.UserFileService;
import service.UserService;
import utils.FileTypeConverter;
import utils.Identifier;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service("userFileService")
public class UserFileServiceImpl implements UserFileService {
    private final UserFileDao userFileDao;
    private final FileDao fileDao;
    private final ReportedFileDao reportedFileDao;
    private final UserService userService;
    private final FileTypeConverter converter;

    public UserFileServiceImpl(UserFileDao userFileDao, FileDao fileDao,ReportedFileDao reportedFileDao, FileTypeConverter converter, UserService userService) {
        this.userFileDao = userFileDao;
        this.fileDao = fileDao;
        this.reportedFileDao = reportedFileDao;
        this.converter = converter;
        this.userService = userService;
    }

    @Override
    public UserFile saveUserFile(User user, UserFile userFile) {
        if(userFile.getFilename().isEmpty()){
            throw new EmptyFilenameException();
        }
        if (userService.authorizeUser(user) == null) {
            throw new UnauthorizedUserException();
        }
        //default root directory
        if (userFile.getDirectory() == null) {
            userFile.setDirectory(0);
        }
        //checking out directory owner if directory not a root directory
        if (userFile.getDirectory() != 0) {
            UserFile uf = userFileDao.findUserFileById(userFile.getDirectory());
            //throw user file owner exception if directory not found or did not belong to user
            if (uf == null || !uf.getUsername().equals(userFile.getUsername())) {
                throw new UserFileOwnerException();
            }
        }
        File file = fileDao.findFileById(userFile.getFileId());
        boolean existed = true;
        //adding file if it is new user file
        if (file == null) {
            file = new File();
            file.setId(userFile.getFileId());
            file.setSize(userFile.getSize());
            if (userFile.getFileStatus() == null)
                file.setStatus(FileStatus.NORMAL);
            else
                file.setStatus(userFile.getFileStatus());
            fileDao.addFile(file);
            existed = false;
        } else if (file.getStatus() == FileStatus.BLOCKED) {
            //do not save user file if the file has been blocked
            throw new FileStatusBlockedException();
        }
        // using FileTypeConverter to convert file type based on user file info
        if(userFile.getType() == null)
            userFile.setType(converter.convertToType(userFile));
        userFile.setUsername(user.getUsername());
        userFile.setCreatedTime(Timestamp.from(Instant.now()));
        if (userFile.getUserFileStatus() == null)
            userFile.setUserFileStatus(UserFileStatus.NORMAL);
        userFileDao.addUserFile(userFile);
        if(existed){
            return null;
        }else{
            return userFile;
        }
    }

    @Override
    public boolean saveUserDirectory(User user, int parentDirectoryId, String directoryName) {
        if (directoryName.isEmpty()) {
            throw new EmptyFilenameException();
        }
        if (userService.authorizeUser(user) == null) {
            throw new UnauthorizedUserException();
        }
        if (parentDirectoryId != 0) {
            UserFile uf = userFileDao.findUserFileById(parentDirectoryId);
            if (uf == null || !uf.getUsername().equals(user.getUsername()) || uf.getUserFileStatus() == UserFileStatus.DELETED) {
                throw new UserFileOwnerException();
            }
        }
        UserFile userFile = new UserFile();
        userFile.setUsername(user.getUsername());
        userFile.setFilename(directoryName);
        userFile.setDirectory(parentDirectoryId);
        userFile.setType(FileTypeConverter.DIRECTORY_TYPE);
        userFile.setCreatedTime(Timestamp.from(Instant.now()));
        userFile.setUserFileStatus(UserFileStatus.NORMAL);
        Identifier identifier = new Identifier("SHA3-256");
        //make up directory file id
        String fileId = userFile.getUsername() + "/" + userFile.getDirectory() + "/" + userFile.getFilename();
        identifier.read(fileId.getBytes(StandardCharsets.UTF_8));
        userFile.setFileId(identifier.getUniqueId());
        File file = fileDao.findFileById(userFile.getFileId());
        //adding directory if it is new user file
        if (file == null) {
            file = new File();
            file.setId(userFile.getFileId());
            file.setSize(0L);
            if (userFile.getFileStatus() == null)
                file.setStatus(FileStatus.NORMAL);
            else
                file.setStatus(userFile.getFileStatus());
            fileDao.addFile(file);
            userFileDao.addUserFile(userFile);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public List<UserFile> getUserFilesByDirectoryId(User user, int directoryId) {
        if (userService.authorizeUser(user) == null) {
            throw new UnauthorizedUserException();
        }
        UserFile userFile = new UserFile();
        userFile.setUsername(user.getUsername());
        userFile.setDirectory(directoryId);
        userFile.setUserFileStatus(UserFileStatus.NORMAL);
        userFile.setFileStatus(FileStatus.NORMAL);
        return userFileDao.findUserFiles(userFile);
    }

    @Override
    public List<UserFile> getUserFilesByWildcardFilename(User user, String filename) {
        if (userService.authorizeUser(user) == null) {
            throw new UnauthorizedUserException();
        }
        UserFile userFile = new UserFile();
        userFile.setUsername(user.getUsername());
        userFile.setFilename(filename);
        userFile.setUserFileStatus(UserFileStatus.NORMAL);
        userFile.setFileStatus(FileStatus.NORMAL);
        return userFileDao.wildcardFindUserFiles(userFile);
    }

    @Override
    public List<UserFile> getUserFilesByWildcardType(User user, String type) {
        if (userService.authorizeUser(user) == null) {
            throw new UnauthorizedUserException();
        }
        UserFile userFile = new UserFile();
        userFile.setUsername(user.getUsername());
        userFile.setType(type);
        userFile.setUserFileStatus(UserFileStatus.NORMAL);
        userFile.setFileStatus(FileStatus.NORMAL);
        return userFileDao.wildcardFindUserFiles(userFile);
    }

    @Override
    public List<UserFile> getRemovedTemporarilyUserFiles(User user) {
        if (userService.authorizeUser(user) == null) {
            throw new UnauthorizedUserException();
        }
        UserFile userFile = new UserFile();
        userFile.setUsername(user.getUsername());
        //make sure only getting deleted and normal file status user files
        userFile.setUserFileStatus(UserFileStatus.DELETED);
        userFile.setFileStatus(FileStatus.NORMAL);
        return userFileDao.findUserFiles(userFile)
                .stream()
                .filter(uf -> {
                    //do not output user file that parent directory has been deleted
                    if (uf.getDirectory() != 0) {
                        uf = userFileDao.findUserFileById(uf.getDirectory());
                        return uf.getUserFileStatus() != UserFileStatus.DELETED;
                    }
                    //get over it if it was root directory
                    return true;
                }).collect(Collectors.toList());
    }

    @Override
    public void removeTemporarilyUserFileById(User user, int id) {
        if (userService.authorizeUser(user) == null) {
            throw new UnauthorizedUserException();
        }
        UserFile uf = userFileDao.findUserFileById(id);
        if (uf == null || !uf.getUsername().equals(user.getUsername()) || uf.getUserFileStatus() != UserFileStatus.NORMAL) {
            throw new UserFileOwnerException();
        }
        UserFile userFile = new UserFile();
        userFile.setUserFileStatus(UserFileStatus.DELETED);
        userFileDao.updateUserFileById(id, userFile);
    }

    @Override
    public void removeTemporarilyUserDirectoryById(User user, int directoryId) {
        if (userService.authorizeUser(user) == null) {
            throw new UnauthorizedUserException();
        }
        UserFile uf = userFileDao.findUserFileById(directoryId);
        //make sure user directory status is normal,no need to move on to next operation if it was not normal status
        if (uf == null || !uf.getUsername().equals(user.getUsername()) || !uf.getType().equals(FileTypeConverter.DIRECTORY_TYPE)
                || uf.getUserFileStatus() != UserFileStatus.NORMAL) {
            throw new UserFileOwnerException();
        }
        //recursively removing temporarily user files
        removeRecursivelyTemporarilyUserDirectory(uf);
    }

    private void removeRecursivelyTemporarilyUserDirectory(UserFile userFile) {
        UserFile uf = new UserFile();
        uf.setDirectory(userFile.getId());
        //make sure user file status is normal
        uf.setUserFileStatus(UserFileStatus.NORMAL);
        userFileDao.findUserFiles(uf).forEach(f -> {
            if (f.getType().equals(FileTypeConverter.DIRECTORY_TYPE)) {
                removeRecursivelyTemporarilyUserDirectory(f);
            } else {
                f.setUserFileStatus(UserFileStatus.DELETED);
                userFileDao.updateUserFileById(f.getId(), f);
            }
        });
        userFile.setUserFileStatus(UserFileStatus.DELETED);
        userFileDao.updateUserFileById(userFile.getId(), userFile);
    }

    @Override
    public void removeUserFileById(User user, int id) {
        User u = userService.authorizeUser(user);
        if (u == null) {
            throw new UnauthorizedUserException();
        }
        UserFile uf = userFileDao.findUserFileById(id);
        if (uf == null || !uf.getUsername().equals(user.getUsername())) {
            throw new UserFileOwnerException();
        }
        userFileDao.deleteUserFileById(id);
    }

    @Override
    public void removeUserDirectoryById(User user, int directoryId) {
        if (userService.authorizeUser(user) == null) {
            throw new UnauthorizedUserException();
        }
        UserFile userFile = userFileDao.findUserFileById(directoryId);
        if (userFile == null || !userFile.getUsername().equals(user.getUsername())
                || !userFile.getType().equals(FileTypeConverter.DIRECTORY_TYPE)) {
            throw new UserFileOwnerException();
        }
        removeRecursivelyUserDirectory(userFile);
    }

    private void removeRecursivelyUserDirectory(UserFile userFile) {
        UserFile uf = new UserFile();
        uf.setDirectory(userFile.getId());
        userFileDao.findUserFiles(uf).forEach(f -> {
            if (f.getType().equals(FileTypeConverter.DIRECTORY_TYPE)) {
                removeRecursivelyUserDirectory(f);
            } else {
                userFileDao.deleteUserFileById(f.getId());
            }
        });
        userFileDao.deleteUserFileById(userFile.getId());
    }

    @Override
    public void renameUserFileById(User user, int id,String filename) {
        if(filename.isEmpty()){
            throw new EmptyFilenameException();
        }
        if (userService.authorizeUser(user) == null) {
            throw new UnauthorizedUserException();
        }
        UserFile uf = userFileDao.findUserFileById(id);
        if (uf == null || !uf.getUsername().equals(user.getUsername()) || uf.getUserFileStatus() != UserFileStatus.NORMAL
                || uf.getFileStatus() != FileStatus.NORMAL) {
            throw new UserFileOwnerException();
        }
        uf.setFilename(filename);
        uf.setCreatedTime(Timestamp.from(Instant.now()));
        userFileDao.updateUserFileById(id,uf);
    }

    @Override
    public ReportedFile reportUserFileByFileId(User user, String fileId,String reason) {
        if (userService.authorizeUser(user) == null) {
            throw new UnauthorizedUserException();
        }
        File file = fileDao.findFileById(fileId);
        if(file == null){
            throw new FileNotExistException();
        }
        if(file.getStatus() == FileStatus.BLOCKED){
            throw new FileStatusBlockedException();
        }
        ReportedFile reportedFile = new ReportedFile();
        reportedFile.setUsername(user.getUsername());
        reportedFile.setFileId(fileId);
        reportedFile.setReason(reason);
        reportedFile.setCreatedTime(Timestamp.from(Instant.now()));
        reportedFileDao.addReportedFile(reportedFile);
        return reportedFile;
    }

    @Override
    public void restoreUserFileById(User user, int id) {
        if (userService.authorizeUser(user) == null) {
            throw new UnauthorizedUserException();
        }
        UserFile uf = userFileDao.findUserFileById(id);
        if (uf == null || !uf.getUsername().equals(user.getUsername()) || uf.getUserFileStatus() != UserFileStatus.DELETED) {
            throw new UserFileOwnerException();
        }
        UserFile userFile = new UserFile();
        userFile.setUserFileStatus(UserFileStatus.NORMAL);
        userFileDao.updateUserFileById(id,userFile);
    }

    @Override
    public void restoreUserDirectoryById(User user, int directoryId) {
        if (userService.authorizeUser(user) == null) {
            throw new UnauthorizedUserException();
        }
        UserFile uf = userFileDao.findUserFileById(directoryId);
        if (uf == null || !uf.getUsername().equals(user.getUsername()) || uf.getUserFileStatus() != UserFileStatus.DELETED) {
            throw new UserFileOwnerException();
        }
        restoreRecursivelyUserDirectory(uf);
    }

    private void restoreRecursivelyUserDirectory(UserFile userFile) {
        UserFile uf = new UserFile();
        uf.setUserFileStatus(UserFileStatus.DELETED);
        uf.setDirectory(userFile.getId());
        userFileDao.findUserFiles(uf).forEach(f -> {
            if (f.getType().equals(FileTypeConverter.DIRECTORY_TYPE)) {
                restoreRecursivelyUserDirectory(f);
            } else {
                f.setUserFileStatus(UserFileStatus.NORMAL);
                userFileDao.updateUserFileById(f.getId(),f);
            }
        });
        userFile.setUserFileStatus(UserFileStatus.NORMAL);
        userFileDao.updateUserFileById(userFile.getId(),userFile);
    }

    @Override
    public void modifyUserFileDirectory(User user, int userFileId, int directoryId) {
        if (userService.authorizeUser(user) == null) {
            throw new UnauthorizedUserException();
        }
        UserFile userFile = userFileDao.findUserFileById(userFileId);
        if (userFile == null || !userFile.getUsername().equals(user.getUsername()) || userFile.getUserFileStatus() != UserFileStatus.NORMAL
                || userFile.getFileStatus() != FileStatus.NORMAL) {
            throw new UserFileOwnerException();
        }
        UserFile userDirectory = userFileDao.findUserFileById(directoryId);
        if (userDirectory == null || !userDirectory.getUsername().equals(user.getUsername()) || userDirectory.getUserFileStatus() != UserFileStatus.NORMAL
                || userDirectory.getFileStatus() != FileStatus.NORMAL || !userDirectory.getType().equals(FileTypeConverter.DIRECTORY_TYPE)) {
            throw new UserFileOwnerException();
        }
        userFile.setDirectory(directoryId);
        userFile.setCreatedTime(Timestamp.from(Instant.now()));
        userDirectory.setCreatedTime(Timestamp.from(Instant.now()));
        userFileDao.updateUserFileById(userFileId,userFile);
        userFileDao.updateUserFileById(directoryId,userDirectory);
    }
}

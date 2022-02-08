package service;

import pojo.ReportedFile;
import pojo.User;
import pojo.UserFile;

import java.util.List;

public interface UserFileService {
    UserFile saveUserFile(User user, UserFile userFile);
    boolean saveUserDirectory(User user,int parentDirectoryId,String directoryName);
    UserFile getUserFileById(User user,int id);
    List<UserFile> getUserFilesByDirectoryId(User user,int directoryId);
    List<UserFile> getUserFilesByWildcardFilename(User user,String filename);
    List<UserFile> getUserFilesByWildcardType(User user,String type);
    List<UserFile> getRemovedTemporarilyUserFiles(User user);
    void removeTemporarilyUserFileById(User user,int id);
    void removeTemporarilyUserDirectoryById(User user,int directoryId);
    void removeUserFileById(User user, int id);
    void removeUserDirectoryById(User user, int directoryId);
    void renameUserFileById(User user,int id,String filename);
    ReportedFile reportUserFileByFileId(User user, String fileId,String reason);
    void restoreUserFileById(User user,int id);
    void restoreUserDirectoryById(User user,int directoryId);
    void modifyUserFileDirectory(User user,int userFileId,int directoryId);
}

package service;

import pojo.*;

import java.util.List;

public interface AdminService {
    List<User> getUsersByWildcardUsername(User user, String username);
    List<User> getUsersByWildcardEmail(User user, String email);
    List<User> getAllUsers(User user);
    List<ReportedFile> getAllReportedFiles(User user);
    void modifyUserStatusByUsername(User user, String username, UserStatus userStatus);
    void modifyUserCapacityByUsername(User user, String username, long capacity);
    void modifyUserRoleByUsername(User user, String username, Role role);
    void modifyUserPasswordByUsername(User user,String username,String password);
    void modifyFileStatusById(User user, String fileId, FileStatus fileStatus);
    void removeFileById(User user,String fileId);
    void removeUserByUsername(User user, String username);
    void removeReportedFileById(User user,Integer reportId);
}

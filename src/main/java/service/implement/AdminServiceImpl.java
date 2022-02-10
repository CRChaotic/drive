package service.implement;

import dao.FileDao;
import dao.ReportedFileDao;
import dao.UserDao;
import exception.RoleException;
import exception.UnauthorizedUserException;
import exception.UserNotExistException;
import org.springframework.stereotype.Service;
import pojo.*;
import service.AdminService;
import service.UserService;

import java.util.List;

@Service("adminService")
public class AdminServiceImpl implements AdminService {
    private final UserDao userDao;
    private final FileDao fileDao;
    private final ReportedFileDao reportedFileDao;
    private final UserService userService;

    public AdminServiceImpl(UserDao userDao, FileDao fileDao,ReportedFileDao reportedFileDao, UserService userService) {
        this.userDao = userDao;
        this.fileDao = fileDao;
        this.reportedFileDao = reportedFileDao;
        this.userService = userService;
    }

    @Override
    public List<User> getUsersByWildcardUsername(User user, String username) {
        user = userService.authorizeUser(user);
        if(user == null){
            throw new UnauthorizedUserException();
        }
        if(user.getRole() != Role.ADMIN){
            throw new RoleException();
        }
        User u = new User();
        u.setUsername(username);
        return userDao.wildcardFindUsers(u);
    }

    @Override
    public List<User> getUsersByWildcardEmail(User user, String email) {
        user = userService.authorizeUser(user);
        if(user == null){
            throw new UnauthorizedUserException();
        }
        if(user.getRole() != Role.ADMIN){
            throw new RoleException();
        }
        User u = new User();
        u.setEmail(email);
        return userDao.wildcardFindUsers(u);
    }

    @Override
    public List<User> getAllUsers(User user) {
        user = userService.authorizeUser(user);
        if(user == null){
            throw new UnauthorizedUserException();
        }
        if(user.getRole() != Role.ADMIN){
            throw new RoleException();
        }
        User u = new User();
        return userDao.wildcardFindUsers(u);
    }

    @Override
    public List<ReportedFile> getAllReportedFiles(User user){
        user = userService.authorizeUser(user);
        if(user == null){
            throw new UnauthorizedUserException();
        }
        if(user.getRole() != Role.ADMIN){
            throw new RoleException();
        }
        return reportedFileDao.findReportedFiles(null);
    }

    @Override
    public ReportedFile getReportedFileById(User user,int id){
        user = userService.authorizeUser(user);
        if(user == null){
            throw new UnauthorizedUserException();
        }
        if(user.getRole() != Role.ADMIN){
            throw new RoleException();
        }
        return reportedFileDao.findReportedFileById(id);
    }

    @Override
    public void modifyUserStatusByUsername(User user, String username, UserStatus userStatus) {
        user = userService.authorizeUser(user);
        if(user == null){
            throw new UnauthorizedUserException();
        }
        if(user.getRole() != Role.ADMIN){
            throw new RoleException();
        }
        User u = userDao.findUserByUsername(username);
        if(u == null){
            throw new UserNotExistException();
        }
        u.setUserStatus(userStatus);
        userDao.updateUserByUsername(username,u);
    }

    @Override
    public void modifyUserCapacityByUsername(User user, String username, long capacity) {
        user = userService.authorizeUser(user);
        if(user == null){
            throw new UnauthorizedUserException();
        }
        if(user.getRole() != Role.ADMIN){
            throw new RoleException();
        }
        User u = userDao.findUserByUsername(username);
        if(u == null){
            throw new UserNotExistException();
        }
        u.setCapacity(capacity);
        userDao.updateUserByUsername(username,u);
    }

    @Override
    public void modifyUserRoleByUsername(User user, String username, Role role) {
        user = userService.authorizeUser(user);
        if(user == null){
            throw new UnauthorizedUserException();
        }
        if(user.getRole() != Role.ADMIN){
            throw new RoleException();
        }
        User u = userDao.findUserByUsername(username);
        if(u == null){
            throw new UserNotExistException();
        }
        u.setRole(role);
        userDao.updateUserByUsername(username,u);
    }

    @Override
    public void modifyUserPasswordByUsername(User user, String username, String password) {
        user = userService.authorizeUser(user);
        if(user == null){
            throw new UnauthorizedUserException();
        }
        if(user.getRole() != Role.ADMIN){
            throw new RoleException();
        }
        User u = userDao.findUserByUsername(username);
        if(u == null){
            throw new UserNotExistException();
        }
        u.setPassword(password);
        userDao.updateUserByUsername(username,u);
    }

    @Override
    public void modifyFileStatusById(User user, String fileId, FileStatus fileStatus) {
        user = userService.authorizeUser(user);
        if(user == null){
            throw new UnauthorizedUserException();
        }
        if(user.getRole() != Role.ADMIN){
            throw new RoleException();
        }
        File file = new File();
        file.setStatus(fileStatus);
        fileDao.updateFileById(fileId,file);
    }

    @Override
    public void removeFileById(User user, String fileId) {
        user = userService.authorizeUser(user);
        if(user == null){
            throw new UnauthorizedUserException();
        }
        if(user.getRole() != Role.ADMIN){
            throw new RoleException();
        }
        fileDao.deleteFileById(fileId);
    }

    @Override
    public void removeUserByUsername(User user, String username) {
        user = userService.authorizeUser(user);
        if(user == null){
            throw new UnauthorizedUserException();
        }
        if(user.getRole() != Role.ADMIN){
            throw new RoleException();
        }
        userDao.deleteUserByUsername(username);
    }

    @Override
    public void removeReportedFileById(User user, Integer reportId) {
        user = userService.authorizeUser(user);
        if(user == null){
            throw new UnauthorizedUserException();
        }
        if(user.getRole() != Role.ADMIN){
            throw new RoleException();
        }
        reportedFileDao.deleteReportedFileById(reportId);
    }
}

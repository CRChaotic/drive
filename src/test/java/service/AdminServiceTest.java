package service;

import dao.DaoFactory;
import dao.FileDao;
import dao.ReportedFileDao;
import dao.UserDao;
import org.junit.Test;
import pojo.*;
import service.implement.AdminServiceImpl;
import service.implement.UserServiceImpl;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

public class AdminServiceTest {
    protected static AdminService adminService;
    protected static  UserService userService;
    static {
        UserDao userDao = DaoFactory.getDaoInstance(UserDao.class);
        FileDao fileDao = DaoFactory.getDaoInstance(FileDao.class);
        ReportedFileDao reportedFileDao = DaoFactory.getDaoInstance(ReportedFileDao.class);
        userService = new UserServiceImpl(userDao);
        adminService = new AdminServiceImpl(userDao,fileDao,reportedFileDao,userService);
    }

    protected static User saveAdminEnv(){
        User user = new User();
        user.setUsername("ryan2");
        user.setPassword("123456");
        user.setEmail("kyle@gmail.com");
        user.setRole(Role.ADMIN);
        user.setCapacity(10240L);
        user.setCreatedTime(Timestamp.from(Instant.now()));
        user.setUserStatus(UserStatus.NORMAL);
        userService.saveUser(user);
        return user;
    }

    @Test
    public void getUsersByWildcardUsername(){
        UserServiceTest.saveUserEnv();
        User admin = saveAdminEnv();
        adminService.getUsersByWildcardUsername(admin,"ryan").forEach(System.out::println);
    }

    @Test
    public void getUsersByWildcardEmail(){
        UserServiceTest.saveUserEnv();
        User admin = saveAdminEnv();
        adminService.getUsersByWildcardEmail(admin,"ryan").forEach(System.out::println);
    }

    @Test
    public void getAllUsers(){
        UserServiceTest.saveUserEnv();
        User admin = saveAdminEnv();
        adminService.getAllUsers(admin).forEach(System.out::println);
    }

    @Test
    public void getAllReportedFiles(){
        UserServiceTest.saveUserEnv();
        User admin = saveAdminEnv();
        UserFileServiceTest.reportUserFileEnv(admin);
        adminService.getAllReportedFiles(admin).forEach(System.out::println);
    }

    @Test
    public void modifyUserStatusByUsername(){
        User user = UserServiceTest.saveUserEnv();
        User admin = saveAdminEnv();
        adminService.getAllUsers(admin).forEach(System.out::println);
        adminService.modifyUserStatusByUsername(admin,user.getUsername(),UserStatus.BLOCKED);
        adminService.getAllUsers(admin).forEach(System.out::println);
    }

    @Test
    public void modifyUserCapacityByUsername(){
        User user = UserServiceTest.saveUserEnv();
        User admin = saveAdminEnv();
        adminService.getAllUsers(admin).forEach(System.out::println);
        adminService.modifyUserCapacityByUsername(admin,user.getUsername(),0L);
        adminService.getAllUsers(admin).forEach(System.out::println);
    }

    @Test
    public void modifyUserRoleByUsername(){
        User user = UserServiceTest.saveUserEnv();
        User admin = saveAdminEnv();
        adminService.getAllUsers(admin).forEach(System.out::println);
        adminService.modifyUserRoleByUsername(admin,user.getUsername(),Role.ADMIN);
        adminService.getAllUsers(admin).forEach(System.out::println);
    }

    @Test
    public void modifyUserPasswordByUsername(){
        User user = UserServiceTest.saveUserEnv();
        User admin = saveAdminEnv();
        adminService.getAllUsers(admin).forEach(System.out::println);
        adminService.modifyUserPasswordByUsername(admin, user.getUsername(), "new_password");
        adminService.getAllUsers(admin).forEach(System.out::println);
    }

    @Test
    public void modifyFileStatusById(){
        User user = UserServiceTest.saveUserEnv();
        UserFile userFile = UserFileServiceTest.saveUserFileEnv(user,0,"test_id","test_file.txt", FileStatus.NORMAL);
        User admin = saveAdminEnv();
        adminService.modifyFileStatusById(admin, userFile.getFileId(),FileStatus.BLOCKED);
    }

    @Test
    public void removeFileById(){
        User user = UserServiceTest.saveUserEnv();
        UserFile userFile = UserFileServiceTest.saveUserFileEnv(user,0,"test_id","test_file.txt", FileStatus.NORMAL);
        User admin = saveAdminEnv();
        adminService.removeFileById(admin,userFile.getFileId());
    }

    @Test
    public void removeUserByUsername(){
        User user = UserServiceTest.saveUserEnv();
        User admin = saveAdminEnv();
        adminService.getAllUsers(admin).forEach(System.out::println);
        adminService.removeUserByUsername(admin,user.getUsername());
        adminService.getAllUsers(admin).forEach(System.out::println);
    }

    @Test
    public void removeReportedFileById(){
        User admin = saveAdminEnv();
        UserFileServiceTest.reportUserFileEnv(admin);
        List<ReportedFile> reportedFiles = adminService.getAllReportedFiles(admin);
        reportedFiles.forEach(System.out::println);
        adminService.removeReportedFileById(admin,reportedFiles.get(1).getId());
    }
}

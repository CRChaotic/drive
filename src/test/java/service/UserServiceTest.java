package service;

import dao.DaoFactory;
import dao.UserDao;
import org.junit.Test;
import pojo.Role;
import pojo.User;
import pojo.UserStatus;
import service.implement.UserServiceImpl;

import java.sql.Timestamp;
import java.time.Instant;

public class UserServiceTest {
    private static final UserService userService;
    static {
        UserDao userDao = DaoFactory.getDaoInstance(UserDao.class);
        userService = new UserServiceImpl(userDao);
    }

    protected static User saveUserEnv(){
        User user = new User();
        user.setUsername("ryan");
        user.setPassword("123456");
        user.setEmail("ryan@gmail.com");
        user.setRole(Role.NORMAL);
        user.setCapacity(10240L);
        user.setCreatedTime(Timestamp.from(Instant.now()));
        user.setUserStatus(UserStatus.NORMAL);
        userService.saveUser(user);
        return user;
    }

    @Test
    public void saveUser(){
        User user = new User();
        user.setUsername("ryan");
        user.setPassword("123456");
        user.setEmail("ryan@gmail.com");
        user.setRole(Role.NORMAL);
        user.setCapacity(10240L);
        user.setCreatedTime(Timestamp.from(Instant.now()));
        user.setUserStatus(UserStatus.NORMAL);
        userService.saveUser(user);
    }

    @Test
    public void authorizeUser(){
        User user = saveUserEnv();
        user = userService.authorizeUser(user);
        System.out.println(user);
    }

    @Test
    public void modifyUserPasswordByUsername(){
        User user = saveUserEnv();
        userService.modifyUserPasswordByUsername(user,"new_password");
    }

    @Test
    public void modifyUserEmailByUsername(){
        User user = saveUserEnv();
        userService.modifyUserEmailByUsername(user,"729256259@qq.com");
    }
}

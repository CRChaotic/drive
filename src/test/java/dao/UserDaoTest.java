package dao;

import org.junit.Test;
import pojo.Role;
import pojo.User;
import pojo.UserStatus;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

public class UserDaoTest {
    private static final UserDao userDao;
    static {
        userDao = DaoFactory.getDaoInstance(UserDao.class);
    }

    protected static User findUserEnv(){
        User user = new User();
        user.setUsername("ryan");
        user.setPassword("123456");
        user.setEmail("ryan@gmail.com");
        user.setRole(Role.NORMAL);
        user.setCapacity(10240L);
        user.setCreatedTime(Timestamp.from(Instant.now()));
        user.setUserStatus(UserStatus.NORMAL);
        userDao.addUser(user);
        return user;
    }

    @Test
    public void addUser(){
        User user = new User();
        user.setUsername("ryan2");
        user.setPassword("123456");
        user.setEmail("ryan2@gmail.com");
        user.setRole(Role.NORMAL);
        user.setCapacity(10240L);
        user.setCreatedTime(Timestamp.from(Instant.now()));
        user.setUserStatus(UserStatus.NORMAL);
        userDao.addUser(user);
    }

    @Test
    public void findUsers(){
        User user = new User();
        user.setUsername("ryan");
        userDao.findUsers(user).forEach(System.out::println);
    }

    @Test
    public void findUserByUsername(){
        findUserEnv();
        System.out.println(userDao.findUserByUsername("ryan"));
    }

    @Test
    public void wildcardFileUsers(){
        User user = new User();
        user.setUsername("ryan");
        userDao.wildcardFindUsers(user).forEach(System.out::println);
        User user1 = new User();
        user1.setRole(Role.NORMAL);
        userDao.wildcardFindUsers(user1).forEach(System.out::println);
    }

    @Test
    public void updateUserByUsername(){
        findUserEnv();
        User u = new User();
        u.setUsername("ryan");
        List<User> userList = userDao.findUsers(u);
        System.out.println(userList.get(0));
        User user = new User();
        user.setPassword("595959");
        userDao.updateUserByUsername("ryan",user);
        System.out.println(userDao.findUsers(u).get(0));
    }

    @Test
    public void deleteUserByUsername(){
        String username = "ryan1";
        userDao.deleteUserByUsername(username);
    }
}

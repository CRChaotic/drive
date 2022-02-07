package dao;

import org.apache.ibatis.annotations.Param;
import pojo.User;

import java.util.List;

public interface UserDao {
    void addUser(User user);
    List<User> findUsers(User user);
    User findUserByUsername(String username);
    List<User> wildcardFindUsers(User user);
    void updateUserByUsername(@Param("username") String username,@Param("user") User user);
    void deleteUserByUsername(String username);
}

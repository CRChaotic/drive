package service;

import pojo.User;

public interface UserService {
    void saveUser(User user);

    User authorizeUser(User user);

    void modifyUserPasswordByUsername(User user, String password);

    void modifyUserEmailByUsername(User user,String email);
}

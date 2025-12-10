package myshop.user.dao;

import myshop.user.model.User;

import java.util.List;

public interface UserDao {
    User createUser(User user);

    List<User> findAllUsers();

    User findById(Long id);

    void updateUser(User user);

    void deleteById(Long id);
}

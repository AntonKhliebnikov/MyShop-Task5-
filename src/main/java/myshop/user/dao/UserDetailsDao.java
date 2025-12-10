package myshop.user.dao;

import myshop.user.model.UserDetails;

import java.util.List;

public interface UserDetailsDao {
    void createUserDetails(UserDetails userDetails);
    List<UserDetails> findAllUserDetails();
    UserDetails findByUserId(Long userId);
    void updateUserDetails(UserDetails userDetails);
    void deleteByUserId(Long userId);
}

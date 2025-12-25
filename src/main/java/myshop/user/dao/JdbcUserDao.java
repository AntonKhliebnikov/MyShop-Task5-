package myshop.user.dao;

import lombok.extern.log4j.Log4j2;
import myshop.common.db.ConnectionManager;
import myshop.common.exception.DaoException;
import myshop.user.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public class JdbcUserDao implements UserDao {

    @Override
    public User createUser(User user) {
        if (user.getId() != null) {
            throw new IllegalArgumentException("When creating a user, the id must be null");
        }

        log.debug("createUser() called with user = {}", user);

        String sql = "INSERT INTO users (username, email) VALUES (?, ?) RETURNING id";
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            log.trace("Preparing request: {}", sql);

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Long generatedId = rs.getLong("id");
                    user.setId(generatedId);
                    log.info("User successfully created, id = {}", generatedId);
                    return user;
                } else {
                    log.error("ResultSet is empty when creating user: {}", user);
                    throw new DaoException("Failed to get generated id for user");

                }
            }
        } catch (SQLException e) {
            log.error("SQL error creating user: {}", user, e);
            throw new DaoException("Error creating user: " + user, e);
        }
    }

    @Override
    public List<User> findAllUsers() {
        String sql = "SELECT id, username, email FROM users";
        log.debug("findAllUsers() called");

        List<User> users = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapRowToUser(rs));
            }

            log.info("{} users found", users.size());
        } catch (SQLException e) {
            log.error("SQL error while getting all users", e);
            throw new DaoException("Error getting list of all users", e);
        }
        return users;
    }

    @Override
    public User findById(Long id) {
        String sql = "SELECT id, username, email FROM users WHERE id = ?";
        log.debug("findById() called with id = {}", id);

        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = mapRowToUser(rs);
                    log.info("User with id = {} found: {}", id, user);
                    return user;
                } else {
                    log.info("User with id = {} not found", id);
                    return null;
                }
            }
        } catch (SQLException e) {
            log.error("SQL error while searching for user by id = {}", id, e);
            throw new DaoException("Error getting user by id = " + id, e);
        }
    }

    @Override
    public void updateUser(User user) {
        if (user.getId() == null) {
            throw new IllegalArgumentException("You can't update a user without an ID");
        }

        String sql = "UPDATE users SET username = ?, email = ? WHERE id = ?";
        log.debug("updateUser() called for user = {}", user);

        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setLong(3, user.getId());
            int updatedRow = ps.executeUpdate();
            if (updatedRow == 0) {
                log.warn("User with id = {} not found, update failed", user.getId());
                throw new DaoException("User with id = " + user.getId()
                        + " not found, update failed");
            }

            log.info("User with id = {} successfully updated", user.getId());
        } catch (SQLException e) {
            log.error("SQL error updating user: {}", user, e);
            throw new DaoException("Error updating user: " + user, e);
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        log.debug("deleteById() called with id = {}", id);

        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            int deletedRow = ps.executeUpdate();
            if (deletedRow == 0) {
                log.warn("User with id = {} not found, deletion failed", id);
                throw new DaoException("User with id = " + id
                        + " not found, deletion failed");
            }

            log.info("User with id = {} successfully deleted", id);
        } catch (SQLException e) {
            log.error("SQL error when deleting user with id = {}", id, e);
            throw new DaoException("Error deleting user by ID = " + id, e);
        }
    }

    private static User mapRowToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("email")
        );
    }
}
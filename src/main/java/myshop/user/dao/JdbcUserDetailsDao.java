package myshop.user.dao;

import lombok.extern.log4j.Log4j2;
import myshop.common.db.ConnectionManager;
import myshop.common.exception.DaoException;
import myshop.user.model.UserDetails;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public class JdbcUserDetailsDao implements UserDetailsDao {

    @Override
    public void createUserDetails(UserDetails userDetails) {
        if (userDetails.getUserId() == null) {
            throw new IllegalArgumentException("When creating userDetails, userId must not be null.");
        }

        String sql = "INSERT INTO user_details (user_id, first_name, last_name, address, phone) " +
                "VALUES (?, ?, ?, ?, ?)";
        log.debug("createUserDetails() called with userDetails = {}", userDetails);

        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userDetails.getUserId());
            ps.setString(2, userDetails.getFirstName());
            ps.setString(3, userDetails.getLastName());
            ps.setString(4, userDetails.getAddress());
            ps.setString(5, userDetails.getPhone());
            int inserted = ps.executeUpdate();
            log.info("userDetails created for userId = {}, rows inserted: {}",
                    userDetails.getUserId(), inserted);
        } catch (SQLException e) {
            log.error("SQL error creating userDetails: {}", userDetails, e);
            throw new DaoException("Error creating userDetails: " + userDetails, e);
        }
    }

    @Override
    public List<UserDetails> findAllUserDetails() {
        String sql = "SELECT user_id, first_name, last_name, address, phone " +
                "FROM user_details";
        log.debug("findAllUserDetails() called");

        List<UserDetails> userDetailsList = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                userDetailsList.add(mapRowToUserDetails(rs));
            }
            log.info("{} entries found in userDetails", userDetailsList.size());
        } catch (SQLException e) {
            log.error("SQL error while retrieving all userDetails", e);
            throw new DaoException("Error retrieving all userDetails", e);
        }
        return userDetailsList;
    }

    @Override
    public UserDetails findByUserId(Long userId) {
        String sql = "SELECT user_id, first_name, last_name, address, phone " +
                "FROM user_details WHERE user_id = ?";
        log.debug("findByUserId() called with userId = {}", userId);

        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UserDetails details = mapRowToUserDetails(rs);
                    log.info("UserDetails for userId = {} found: {}", userId, details);
                    return details;
                } else {
                    log.info("UserDetails for userId = {} not found", userId);
                    return null;
                }
            }
        } catch (SQLException e) {
            log.error("SQL error while retrieving userDetails by userId = {}", userId, e);
            throw new DaoException("Error retrieving userDetails by userId = " + userId, e);
        }
    }

    @Override
    public void updateUserDetails(UserDetails userDetails) {
        if (userDetails.getUserId() == null) {
            throw new IllegalArgumentException("Cannot update userDetails without userId");
        }

        String sql = "UPDATE user_details SET first_name = ?, last_name = ?, address = ?, phone = ? " +
                "WHERE user_id = ?";
        log.debug("updateUserDetails() called with userDetails = {}", userDetails);

        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, userDetails.getFirstName());
            ps.setString(2, userDetails.getLastName());
            ps.setString(3, userDetails.getAddress());
            ps.setString(4, userDetails.getPhone());
            ps.setLong(5, userDetails.getUserId());
            int updatedRow = ps.executeUpdate();
            if (updatedRow == 0) {
                log.warn("UserDetails with userId = {} not found, update failed", userDetails.getUserId());
                throw new DaoException("UserDetails with userId = " + userDetails.getUserId()
                        + " not found, update failed");
            }

            log.info("UserDetails with userId = {} has been updated successfully", userDetails.getUserId());
        } catch (SQLException e) {
            log.error("SQL error updating userDetails = {}", userDetails, e);
            throw new DaoException("Error updating userDetails " + userDetails, e);
        }
    }

    @Override
    public void deleteByUserId(Long userId) {
        String sql = "DELETE FROM user_details WHERE user_id = ?";
        log.debug("deleteByUserId() called with userId = {}", userId);

        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            int deletedRow = ps.executeUpdate();
            if (deletedRow == 0) {
                log.warn("UserDetails with userId = {} not found, deletion failed", userId);
                throw new DaoException("UserDetails with userId = " + userId
                        + " not found, deletion failed");
            }

            log.info("UserDetails with userId = {} successfully deleted", userId);
        } catch (SQLException e) {
            log.error("SQL error deleting userDetails by userId = {}", userId, e);
            throw new DaoException("Error deleting userDetails by userId = " + userId, e);
        }
    }

    private static UserDetails mapRowToUserDetails(ResultSet rs) throws SQLException {
        return new UserDetails(
                rs.getLong("user_id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("address"),
                rs.getString("phone")
        );
    }
}
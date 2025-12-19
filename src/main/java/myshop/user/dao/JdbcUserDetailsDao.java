package myshop.user.dao;

import myshop.common.db.ConnectionManager;
import myshop.common.exception.DaoException;
import myshop.user.model.UserDetails;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcUserDetailsDao implements UserDetailsDao {
    @Override
    public void createUserDetails(UserDetails userDetails) {
        if (userDetails.getUserId() == null) {
            throw new IllegalArgumentException("При создании данных пользователя userId не должен быть null");
        }

        String sql = "INSERT INTO user_details (user_id, first_name, last_name, address, phone) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userDetails.getUserId());
            ps.setString(2, userDetails.getFirstName());
            ps.setString(3, userDetails.getLastName());
            ps.setString(4, userDetails.getAddress());
            ps.setString(5, userDetails.getPhone());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Ошибка при создании данных пользователя: " + userDetails, e);
        }
    }

    @Override
    public List<UserDetails> findAllUserDetails() {
        String sql = "SELECT user_id, first_name, last_name, address, phone " +
                "FROM user_details";
        List<UserDetails> userDetailsList = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                userDetailsList.add(mapRowToUserDetails(rs));

            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка при получении всех данных пользователей", e);
        }
        return userDetailsList;
    }

    @Override
    public UserDetails findByUserId(Long userId) {
        String sql = "SELECT user_id, first_name, last_name, address, phone " +
                "FROM user_details WHERE user_id = ?";
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUserDetails(rs);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка при получении данных пользователя по userId = " + userId, e);
        }
    }

    @Override
    public void updateUserDetails(UserDetails userDetails) {
        if (userDetails.getUserId() == null) {
            throw new IllegalArgumentException("Нельзя обновить данные пользователя без user_id");
        }

        String sql = "UPDATE user_details SET first_name = ?, last_name = ?, address = ?, phone = ? " +
                "WHERE user_id = ?";
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, userDetails.getFirstName());
            ps.setString(2, userDetails.getLastName());
            ps.setString(3, userDetails.getAddress());
            ps.setString(4, userDetails.getPhone());
            ps.setLong(5, userDetails.getUserId());
            int updatedRow = ps.executeUpdate();
            if (updatedRow == 0) {
                throw new DaoException("Данные пользователя с user_id = " + userDetails.getUserId()
                        + " не найдены, обновление не выполнено");
            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка при обновлении данных пользователя " + userDetails, e);
        }
    }

    @Override
    public void deleteByUserId(Long userId) {
        String sql = "DELETE FROM user_details WHERE user_id = ?";
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            int deletedRow = ps.executeUpdate();
            if (deletedRow == 0) {
                throw new DaoException("Данные пользователя с userId = " + userId
                        + " не найдены, удаление не выполнено");
            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка при удалении данных пользователя по userId = " + userId, e);
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
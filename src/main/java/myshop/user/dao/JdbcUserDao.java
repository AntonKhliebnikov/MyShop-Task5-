package myshop.user.dao;

import myshop.common.db.ConnectionManager;
import myshop.common.exception.DaoException;
import myshop.user.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcUserDao implements UserDao {

    @Override
    public User createUser(User user) {
        if (user.getId() != null) {
            throw new IllegalArgumentException("При создании пользователя id должен быть null");
        }

        String sql = "INSERT INTO users (username, email) VALUES (?, ?) RETURNING id";
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Long generatedId = rs.getLong("id");
                    user.setId(generatedId);
                    return user;
                } else {
                    throw new DaoException("Не удалось получить сгенерированный id для пользователя");
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка при создании пользователя: " + user, e);
        }
    }

    @Override
    public List<User> findAllUsers() {
        String sql = "SELECT id, username, email FROM users";
        List<User> users = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(mapRowToUser(rs));
            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка при получении списка всех пользователей", e);
        }
        return users;
    }

    @Override
    public User findById(Long id) {
        String sql = "SELECT id, username, email FROM users WHERE id = ?";
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка при получении пользователя по id = " + id, e);
        }
    }

    @Override
    public void updateUser(User user) {
        if (user.getId() == null) {
            throw new IllegalArgumentException("Нельзя обновить пользователя без id");
        }

        String sql = "UPDATE users SET username = ?, email = ? WHERE id = ?";
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setLong(3, user.getId());
            int updatedRow = ps.executeUpdate();
            if (updatedRow == 0) {
                throw new DaoException("Пользователь с id = " + user.getId()
                        + " не найден, обновление не выполнено");
            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка при обновлении пользователя: " + user, e);
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            int deletedRow = ps.executeUpdate();
            if (deletedRow == 0) {
                throw new DaoException("Пользователь с id = " + id
                        + " не найден, удаление не выполнено");
            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка при удалении пользователя по id = " + id, e);
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
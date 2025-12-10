package myshop.order.dao;

import myshop.common.db.ConnectionManager;
import myshop.common.exception.DaoException;
import myshop.order.model.Order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcOrderDao implements OrderDao {
    @Override
    public Order saveOrder(Order order) {
        if (order.getId() != null) {
            throw new IllegalArgumentException("При создании заказа id должен быть null");
        }
        String sql = "INSERT INTO orders (user_id, ordered_products, total_amount) " +
                "VALUES (?, ?, ?) RETURNING id";
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, order.getUserId());
            ps.setString(2, order.getOrderedProducts());
            ps.setBigDecimal(3, order.getTotalAmount());
            try(ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Long generatedId = rs.getLong("id");
                    order.setId(generatedId);
                    return order;
                } else {
                    throw new DaoException("Не удалось получить сгенерированный id для заказа");
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка при сохранении заказа: " + order, e);
        }
    }

    @Override
    public List<Order> findAllOrdersByUserId(Long userId) {
        String sql = "SELECT id, user_id, ordered_products, total_amount FROM orders WHERE user_id = ?";
        List<Order> userOrders = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    userOrders.add(mapRowToOrder(rs));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка при получении всех заказов пользователя по userId = " + userId, e);
        }
        return userOrders;
    }

    @Override
    public List<Order> findAllOrders() {
        String sql = "SELECT id, user_id, ordered_products, total_amount FROM orders";
        List<Order> orders = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                orders.add(mapRowToOrder(rs));
            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка при получении всех заказов", e);
        }
        return orders;
    }

    private static Order mapRowToOrder(ResultSet rs) throws SQLException {
        return new Order(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("ordered_products"),
                rs.getBigDecimal("total_amount")
        );
    }
}
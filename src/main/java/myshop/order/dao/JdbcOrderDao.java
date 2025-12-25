package myshop.order.dao;

import lombok.extern.log4j.Log4j2;
import myshop.common.db.ConnectionManager;
import myshop.common.exception.DaoException;
import myshop.order.model.Order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public class JdbcOrderDao implements OrderDao {
    @Override
    public Order saveOrder(Order order) {
        if (order.getId() != null) {
            throw new IllegalArgumentException("When creating an order, the id must be null");
        }
        String sql = "INSERT INTO orders (user_id, ordered_products, total_amount) " +
                "VALUES (?, ?, ?) RETURNING id";

        log.debug("saveOrder() called with order = {}", order);

        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, order.getUserId());
            ps.setString(2, order.getOrderedProducts());
            ps.setBigDecimal(3, order.getTotalAmount());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Long generatedId = rs.getLong("id");
                    order.setId(generatedId);
                    log.info("Order successfully created, id = {}", generatedId);
                    return order;
                } else {
                    log.error("ResultSet is empty when creating order: {}", order);
                    throw new DaoException("Failed to get generated id for order");
                }
            }
        } catch (SQLException e) {
            log.error("SQL error saving order: {}", order, e);
            throw new DaoException("Error saving order: " + order, e);
        }
    }

    @Override
    public List<Order> findAllOrdersByUserId(Long userId) {
        String sql = "SELECT id, user_id, ordered_products, total_amount FROM orders WHERE user_id = ?";
        log.debug("findAllOrdersByUserId() called with userId = {}", userId);

        List<Order> userOrders = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    userOrders.add(mapRowToOrder(rs));
                }
                log.info("{} userOrders found", userOrders.size());
            }
        } catch (SQLException e) {
            log.error("SQL error getting all userOrders by userId = {}", userId, e);
            throw new DaoException("Error getting all userOrders by userId = " + userId, e);
        }
        return userOrders;
    }

    @Override
    public List<Order> findAllOrders() {
        String sql = "SELECT id, user_id, ordered_products, total_amount FROM orders";
        log.debug("findAllOrders() called");

        List<Order> orders = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                orders.add(mapRowToOrder(rs));
            }
            log.info("{} orders found", orders.size());
        } catch (SQLException e) {
            log.error("SQL error receiving all orders", e);
            throw new DaoException("Error receiving all orders", e);
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
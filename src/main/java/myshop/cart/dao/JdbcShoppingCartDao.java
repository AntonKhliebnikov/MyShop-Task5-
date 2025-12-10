package myshop.cart.dao;

import myshop.cart.model.ShoppingCart;
import myshop.common.db.ConnectionManager;
import myshop.common.exception.DaoException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcShoppingCartDao implements ShoppingCartDao {
    @Override
    public void addProduct(Long userId, Long productId, Integer quantity) {
        String sql = "INSERT INTO shopping_cart (user_id, product_id, quantity) VALUES (?, ?, ?)";
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            ps.setInt(3, quantity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Ошибка при добавлении продукта в корзину", e);
        }
    }

    @Override
    public void removeProduct(Long userId, Long productId) {
        String sql = "DELETE FROM shopping_cart WHERE user_id = ? AND product_id = ?";
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            int deletedRows = ps.executeUpdate();
            if (deletedRows == 0) {
                throw new DaoException("Товар не найден в корзине пользователя, удаление не выполнено");
            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка при удалении продукта из корзины", e);
        }
    }

    @Override
    public List<ShoppingCart> findByUserId(Long userId) {
        String sql = "SELECT user_id, product_id, quantity FROM shopping_cart WHERE user_id = ?";
        List<ShoppingCart> cartItems = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cartItems.add(mapRowToShoppingCart(rs));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка при получении корзины пользователя по userId = " + userId, e);
        }
        return cartItems;
    }

    @Override
    public void clearCart(Long userId) {
        String sql = "DELETE FROM shopping_cart WHERE user_id = ?";
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Ошибка при удалении корзины пользователя по userId = " + userId, e);
        }
    }

    private static ShoppingCart mapRowToShoppingCart(ResultSet rs) throws SQLException {
        return new ShoppingCart(
                rs.getLong("user_id"),
                rs.getLong("product_id"),
                rs.getInt("quantity")
        );
    }
}
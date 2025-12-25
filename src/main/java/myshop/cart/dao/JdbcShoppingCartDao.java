package myshop.cart.dao;

import lombok.extern.log4j.Log4j2;
import myshop.cart.model.ShoppingCart;
import myshop.common.db.ConnectionManager;
import myshop.common.exception.DaoException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public class JdbcShoppingCartDao implements ShoppingCartDao {

    @Override
    public void addProduct(Long userId, Long productId, Integer quantity) {
        String sql = "INSERT INTO shopping_cart (user_id, product_id, quantity) VALUES (?, ?, ?)";
        log.debug("addProduct() called with userId = {}, productId = {}, quantity = {}.",
                userId, productId, quantity);

        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            ps.setInt(3, quantity);
            int inserted = ps.executeUpdate();
            log.info("Products added to cart by userId = {}, productId = {}, quantity = {}, rows inserted: {}.",
                    userId, productId, quantity, inserted);
        } catch (SQLException e) {
            log.error("SQL error when adding product to cart.", e);
            throw new DaoException("Error when adding product to cart", e);
        }
    }

    @Override
    public void removeProduct(Long userId, Long productId) {
        String sql = "DELETE FROM shopping_cart WHERE user_id = ? AND product_id = ?";
        log.debug("removeProduct() called with userId = {}, productId = {}.", userId, productId);

        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, productId);
            int deletedRows = ps.executeUpdate();
            if (deletedRows == 0) {
                log.warn("The product with productId = {} was not found" +
                                " in the user's cart with userId = {}," +
                                " deletion failed.",
                        productId, userId);
                throw new DaoException("The product was not found in the user's cart and deletion failed.");
            }

            log.info("The product with productId = {} was successfully removed from the user's cart with userId = {}.",
                    productId, userId);
        } catch (SQLException e) {
            log.error("SQL error deleting product with productId = {} from user's cart with userId = {}.",
                    productId, userId, e);
            throw new DaoException("Error deleting product from user's cart.", e);
        }
    }

    @Override
    public List<ShoppingCart> findByUserId(Long userId) {
        String sql = "SELECT user_id, product_id, quantity FROM shopping_cart WHERE user_id = ?";
        log.debug("findByUserId() called with userId = {}.", userId);

        List<ShoppingCart> cartItems = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cartItems.add(mapRowToShoppingCart(rs));
                }

                log.info("{} products found in user's cart.", cartItems.size());
            }
        } catch (SQLException e) {
            log.error("SQL error retrieving user's cart by userId = {}.", userId, e);
            throw new DaoException("Error retrieving user's cart by userId = " + userId, e);
        }
        return cartItems;
    }

    @Override
    public void clearCart(Long userId) {
        String sql = "DELETE FROM shopping_cart WHERE user_id = ?";
        log.debug("clearCart() called with userId = {}.", userId);

        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.executeUpdate();

            log.info("The user's cart with userId = {} has been successfully emptied.", userId);
        } catch (SQLException e) {
            log.error("SQL error deleting user's cart by userId = {}.", userId, e);
            throw new DaoException("Error deleting user's cart by userId = " + userId, e);
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
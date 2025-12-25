package myshop.product.dao;

import lombok.extern.log4j.Log4j2;
import myshop.common.db.ConnectionManager;
import myshop.common.exception.DaoException;
import myshop.product.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public class JdbcProductDao implements ProductDao {

    @Override
    public Product createProduct(Product product) {
        if (product.getId() != null) {
            throw new IllegalArgumentException("When creating a product, the id must be null");
        }

        String sql = "INSERT INTO products (product_name, price) VALUES (?, ?) RETURNING id";

        log.debug("createProduct() called with product = {}", product);

        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, product.getProductName());
            ps.setBigDecimal(2, product.getPrice());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Long generatedId = rs.getLong("id");
                    product.setId(generatedId);
                    log.info("Product successfully created, id = {}", generatedId);
                    return product;
                } else {
                    log.error("ResultSet is empty when creating product: {}", product);
                    throw new DaoException("Failed to get generated id for product");
                }
            }
        } catch (SQLException e) {
            log.error("SQL error creating product: {}", product, e);
            throw new DaoException("Error creating product: " + product, e);
        }
    }

    @Override
    public List<Product> findAllProducts() {
        String sql = "SELECT id, product_name, price FROM products";
        log.debug("findAllProducts() called");

        List<Product> products = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                products.add(mapRowToProduct(rs));
            }

            log.info("{} products found", products.size());
        } catch (SQLException e) {
            log.error("SQL error while getting all products", e);
            throw new DaoException("Error receiving all products", e);
        }
        return products;
    }

    @Override
    public Product findById(Long id) {
        String sql = "SELECT id, product_name, price FROM products WHERE id = ?";
        log.debug("findById() called with id = {}", id);

        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Product product = mapRowToProduct(rs);
                    log.info("Product with id = {} found: {}", id, product);
                    return product;
                } else {
                    log.info("Product with id = {} not found", id);
                    return null;
                }
            }
        } catch (SQLException e) {
            log.error("SQL error while searching for product by id = {}", id, e);
            throw new DaoException("Error getting product by ID = " + id, e);
        }
    }

    @Override
    public void updateProduct(Product product) {
        if (product.getId() == null) {
            throw new IllegalArgumentException("You cannot update a product without an ID");
        }

        String sql = "UPDATE products SET product_name = ?, price = ? WHERE id = ?";
        log.debug("updateProduct() called with product = {}", product);

        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, product.getProductName());
            ps.setBigDecimal(2, product.getPrice());
            ps.setLong(3, product.getId());
            int updatedRow = ps.executeUpdate();
            if (updatedRow == 0) {
                log.warn("Product with id = {} not found, update failed", product.getId());
                throw new DaoException("Product with id = " + product.getId() +
                        " not found, update failed");
            }

            log.info("Product with id = {} successfully updated", product.getId());
        } catch (SQLException e) {
            log.error("SQL error updating product: {}", product, e);
            throw new DaoException("Error updating product: " + product, e);
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM products WHERE id = ?";
        log.debug("deleteById() called with id = {}", id);

        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            int deletedRow = ps.executeUpdate();
            if (deletedRow == 0) {
                log.warn("Product with id = {} not found, deletion failed", id);
                throw new DaoException("Product with id = " + id +
                        " not found, deletion failed");
            }

            log.info("Product with id = {} successfully deleted", id );
        } catch (SQLException e) {
            log.error("SQL error when deleting product with id = {}", id, e);
            throw new DaoException("Error deleting product by ID = " + id, e);
        }
    }

    private static Product mapRowToProduct(ResultSet rs) throws SQLException {
        return new Product(
                rs.getLong("id"),
                rs.getString("product_name"),
                rs.getBigDecimal("price")
        );
    }
}
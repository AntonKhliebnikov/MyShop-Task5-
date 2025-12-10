package myshop.product.dao;

import myshop.common.db.ConnectionManager;
import myshop.common.exception.DaoException;
import myshop.product.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcProductDao implements ProductDao {
    @Override
    public Product createProduct(Product product) {
        if (product.getId() != null) {
            throw new IllegalArgumentException("При создании продукта id должен быть null");
        }

        String sql = "INSERT INTO products (product_name, price) VALUES (?, ?) RETURNING id";
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, product.getProductName());
            ps.setBigDecimal(2, product.getPrice());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Long generatedId = rs.getLong("id");
                    product.setId(generatedId);
                    return product;
                } else {
                    throw new DaoException("Не удалось получить сгенерированный id для продукта");
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка при создании продукта: " + product, e);
        }
    }

    @Override
    public List<Product> findAllProducts() {
        String sql = "SELECT id, product_name, price FROM products";
        List<Product> products = new ArrayList<>();
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                products.add(mapRowToProduct(rs));
            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка при получении всех продуктов", e);
        }
        return products;
    }

    @Override
    public Product findById(Long id) {
        String sql = "SELECT id, product_name, price FROM products WHERE id = ?";
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToProduct(rs);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка при получении продукта по id = " + id, e);
        }
    }

    @Override
    public void updateProduct(Product product) {
        if (product.getId() == null) {
            throw new IllegalArgumentException("Нельзя обновить продукт без id");
        }

        String sql = "UPDATE products SET product_name = ?, price = ? WHERE id = ?";
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, product.getProductName());
            ps.setBigDecimal(2, product.getPrice());
            ps.setLong(3, product.getId());
            int updatedRow = ps.executeUpdate();
            if (updatedRow == 0) {
                throw new DaoException("Продукт с id = " + product.getId() +
                        " не найден, обновление не выполнено");
            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка при обновлении продукта: " + product, e);
        }
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            int deletedRow = ps.executeUpdate();
            if (deletedRow == 0) {
                throw new DaoException("Продукт с id = " + id +
                        " не найден, удаление не выполнено");
            }
        } catch (SQLException e) {
            throw new DaoException("Ошибка при удалении продукта по id = " + id, e);
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
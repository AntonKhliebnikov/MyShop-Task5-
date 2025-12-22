package myshop.product.dao;

import myshop.cart.dao.JdbcShoppingCartDao;
import myshop.cart.dao.ShoppingCartDao;
import myshop.common.db.ConnectionManager;
import myshop.common.exception.DaoException;
import myshop.product.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class JdbcProductDaoTest {
    private ProductDao productDao;

    @BeforeEach
    void setUp() throws SQLException {
        productDao = new JdbcProductDao();
        try (Connection connection = ConnectionManager.getConnection();
             Statement st = connection.createStatement()) {
            st.executeUpdate("DELETE FROM shopping_cart");
            st.executeUpdate("DELETE FROM products");
        }
    }

    @Test
    void createProduct_checkThatThrowIllegalArgumentExceptionWhenProductIdNotNull() {
        Product product = new Product(1L, "test_product_name", new BigDecimal("1000.00"));
        assertThrows(IllegalArgumentException.class,
                () -> productDao.createProduct(product),
                "When creating a product with an already defined id, there should be an IllegalArgumentException"
        );
    }

    @Test
    void createProduct_checkThatProductIsSavedAndAssignedAnId() {
        Product product = new Product();
        product.setProductName("test_product_name");
        product.setPrice(new BigDecimal("1000.00"));
        Product createdProduct = productDao.createProduct(product);
        assertNotNull(createdProduct.getId(), "Once created, the product must have an ID.");
        assertEquals("test_product_name", createdProduct.getProductName());
        assertEquals(new BigDecimal("1000.00"), createdProduct.getPrice());
    }

    @Test
    void createProduct_checkThatProductInDb() {
        Product product = new Product();
        product.setProductName("test_product_name");
        product.setPrice(new BigDecimal("1000.00"));
        Product createdProduct = productDao.createProduct(product);
        Product productFromDb = productDao.findById(createdProduct.getId());
        assertNotNull(productFromDb.getId(), "The product must exist in the database.");
        assertEquals("test_product_name", productFromDb.getProductName());
        assertEquals(new BigDecimal("1000.00"), productFromDb.getPrice());
    }

    @Test
    void createProduct_checkThatThrowDaoExceptionWhenResultSetIsEmpty() throws SQLException {
        Product product = new Product();
        product.setProductName("test_product_name");
        product.setPrice(new BigDecimal("1000.00"));

        try (MockedStatic<ConnectionManager> mockedStatic = Mockito.mockStatic(ConnectionManager.class)) {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            ResultSet mockRs = mock(ResultSet.class);
            mockedStatic.when(ConnectionManager::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPs);
            when(mockPs.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(false);
            assertThrows(DaoException.class,
                    () -> productDao.createProduct(product),
                    "If ResultSet is empty, a DaoException must be thrown."
            );

            verify(mockPs).executeQuery();
        }
    }

    @Test
    void createProduct_checkThatWrapSqlExceptionIntoDaoException() {
        Product product = new Product();
        product.setProductName("test_product_name");
        product.setPrice(new BigDecimal("1000.00"));

        try (MockedStatic<ConnectionManager> mockedStatic = Mockito.mockStatic(ConnectionManager.class)) {
            mockedStatic.when(ConnectionManager::getConnection)
                    .thenThrow(new SQLException("Test SQL error"));

            assertThrows(DaoException.class,
                    () -> productDao.createProduct(product),
                    "When SQLException is thrown, DaoException must be thrown."
            );
        }
    }

    @Test
    void findAllProducts_checkThatReturnListProducts() {
        Product product1 = new Product();
        product1.setProductName("test_product_name1");
        product1.setPrice(new BigDecimal("1000.00"));
        productDao.createProduct(product1);

        Product product2 = new Product();
        product2.setProductName("test_product_name2");
        product2.setPrice(new BigDecimal("2000.00"));
        productDao.createProduct(product2);

        List<Product> productList = productDao.findAllProducts();
        assertEquals(2, productList.size(), "2 products should be returned");
        boolean testProduct1 = productList.stream()
                .anyMatch(p -> p.getProductName().equals("test_product_name1")
                        && p.getPrice().equals(new BigDecimal("1000.00"))
                );

        boolean testProduct2 = productList.stream()
                .anyMatch(p -> p.getProductName().equals("test_product_name2")
                        && p.getPrice().equals(new BigDecimal("2000.00"))
                );

        assertTrue(testProduct1, "There should be testProduct1 among the products");
        assertTrue(testProduct2, "There should be testProduct2 among the products");
    }

    @Test
    void findAllProducts_checkThatWrapSqlExceptionIntoDaoException() {
        Product product1 = new Product();
        product1.setProductName("test_product_name1");
        product1.setPrice(new BigDecimal("1000.00"));
        productDao.createProduct(product1);

        Product product2 = new Product();
        product2.setProductName("test_product_name2");
        product2.setPrice(new BigDecimal("2000.00"));
        productDao.createProduct(product2);

        try (MockedStatic<ConnectionManager> mockedStatic = Mockito.mockStatic(ConnectionManager.class)) {
            mockedStatic.when(ConnectionManager::getConnection)
                    .thenThrow(new SQLException("Test SQL error"));

            assertThrows(DaoException.class,
                    () -> productDao.findAllProducts(),
                    "When SQLException is thrown, DaoException must be thrown."
            );
        }
    }

    @Test
    void findById_checkThatReturnNullWhenProductDoesNotExist() {
        Long notExistingId = Long.MAX_VALUE;
        Product productById = productDao.findById(notExistingId);
        assertNull(productById, "If the product is not in the database, findById should return null.");
    }

    @Test
    void findById_checkThatWrapSqlExceptionIntoDaoException() {
        Product product = new Product();
        product.setProductName("test_product_name");
        product.setPrice(new BigDecimal("1000.00"));
        Product createdProduct = productDao.createProduct(product);

        try (MockedStatic<ConnectionManager> mockedStatic = Mockito.mockStatic(ConnectionManager.class)) {
            mockedStatic.when(ConnectionManager::getConnection)
                    .thenThrow(new SQLException("Test SQL error"));

            assertThrows(DaoException.class,
                    () -> productDao.findById(createdProduct.getId()),
                    "When SQLException is thrown, DaoException must be thrown."
            );
        }
    }

    @Test
    void updateProduct_checkThatThrowIllegalArgumentExceptionWhenProductIdIsNull() {
        Product product = new Product();
        product.setProductName("test_product_name");
        product.setPrice(new BigDecimal("1000.00"));

        assertThrows(IllegalArgumentException.class,
                () -> productDao.updateProduct(product),
                "Updating a product without an id should throw an IllegalArgumentException"
        );
    }

    @Test
    void updateProduct_checkThatChangeProductData() {
        Product product = new Product();
        product.setProductName("test_product_name");
        product.setPrice(new BigDecimal("1000.00"));
        productDao.createProduct(product);

        product.setPrice(new BigDecimal("2000.00"));
        productDao.updateProduct(product);
        Product productFromDb = productDao.findById(product.getId());

        assertNotNull(productFromDb, "After the update the product should exist");
        assertEquals("test_product_name", productFromDb.getProductName());
        assertEquals(new BigDecimal("2000.00"), productFromDb.getPrice());
    }

    @Test
    void updateProduct_checkThatThrowDaoExceptionWhenProductNotFoundById() {
        Product product = new Product();
        product.setId(Long.MAX_VALUE);
        product.setProductName("test_product_name");
        product.setPrice(new BigDecimal("1000.00"));

        assertThrows(DaoException.class,
                () -> productDao.updateProduct(product),
                "If the product is not found by id, there should be a DaoException"
        );
    }

    @Test
    void updateProduct_checkThatWrapSqlExceptionIntoDaoException() {
        Product product = new Product();
        product.setProductName("test_product_name");
        product.setPrice(new BigDecimal("1000.00"));
        productDao.createProduct(product);

        try (MockedStatic<ConnectionManager> mockedStatic = Mockito.mockStatic(ConnectionManager.class)) {
            mockedStatic.when(ConnectionManager::getConnection)
                    .thenThrow(new SQLException("Test SQL error"));

            assertThrows(DaoException.class,
                    () -> productDao.updateProduct(product),
                    "When SQLException is thrown, DaoException must be thrown."
            );
        }
    }

    @Test
    void deleteById_checkThatProductRemoved() {
        Product product = new Product();
        product.setProductName("test_product_name");
        product.setPrice(new BigDecimal("1000.00"));
        productDao.createProduct(product);

        Long id = product.getId();
        productDao.deleteById(id);
        Product deletedProduct = productDao.findById(id);

        assertNull(deletedProduct, "After deletion, the product should not exist in the database.");
    }

    @Test
    void deleteById_checkThatThrowDaoExceptionWhenProductWasNotDeleteById() {
        Long notExistingId = Long.MAX_VALUE;

        assertThrows(DaoException.class,
                () -> productDao.deleteById(notExistingId),
                "If the product is not found by id, there should be a DaoException"
        );
    }

    @Test
    void deleteById_checkThatWrapSqlExceptionIntoDaoException() {
        Product product = new Product();
        product.setProductName("test_product_name");
        product.setPrice(new BigDecimal("1000.00"));
        productDao.createProduct(product);

        try (MockedStatic<ConnectionManager> mockedStatic = Mockito.mockStatic(ConnectionManager.class)) {
            mockedStatic.when(ConnectionManager::getConnection)
                    .thenThrow(new SQLException("Test SQL error"));

            assertThrows(DaoException.class,
                    () -> productDao.deleteById(product.getId()),
                    "When SQLException is thrown, DaoException must be thrown."
            );
        }
    }
}
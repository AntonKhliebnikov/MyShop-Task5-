package myshop.cart.dao;

import myshop.cart.model.ShoppingCart;
import myshop.common.db.ConnectionManager;
import myshop.common.exception.DaoException;
import myshop.product.dao.JdbcProductDao;
import myshop.product.dao.ProductDao;
import myshop.product.model.Product;
import myshop.user.dao.JdbcUserDao;
import myshop.user.dao.UserDao;
import myshop.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JdbcShoppingCartDaoTest {
    private ShoppingCartDao shoppingCartDao;
    private Long userId1;
    private Long userId2;
    private Long productId1;
    private Long productId2;

    @BeforeEach
    void setUp() throws SQLException {
        shoppingCartDao = new JdbcShoppingCartDao();
        UserDao userDao = new JdbcUserDao();
        ProductDao productDao = new JdbcProductDao();

        try (Connection connection = ConnectionManager.getConnection();
             Statement st = connection.createStatement()) {
            st.executeUpdate("DELETE FROM shopping_cart");
            st.executeUpdate("DELETE FROM users");
            st.executeUpdate("DELETE FROM products");
        }

        User user1 = new User();
        user1.setUsername("test_user1");
        user1.setEmail("testuser1@gmail.com");
        userId1 = userDao.createUser(user1).getId();

        User user2 = new User();
        user2.setUsername("test_user2");
        user2.setEmail("testuser2@gmail.com");
        userId2 = userDao.createUser(user2).getId();

        Product product1 = new Product();
        product1.setProductName("test_product_name1");
        product1.setPrice(new BigDecimal("1000.00"));
        productId1 = productDao.createProduct(product1).getId();

        Product product2 = new Product();
        product2.setProductName("test_product_name2");
        product2.setPrice(new BigDecimal("2000.00"));
        productId2 = productDao.createProduct(product2).getId();
    }

    @Test
    void addProduct_checkThatProductAddToShoppingCart() {
        shoppingCartDao.addProduct(userId1, productId1, 1);
        List<ShoppingCart> productsList = shoppingCartDao.findByUserId(userId1);
        assertEquals(1, productsList.size(),
                "Shopping cart must contain one product");

        ShoppingCart productInShoppingCart = productsList.getFirst();

        assertEquals(userId1, productInShoppingCart.getUserId());
        assertEquals(productId1, productInShoppingCart.getProductId());
        assertEquals(1, productInShoppingCart.getQuantity());
    }

    @Test
    void addProduct_checkThatWrapSqlExceptionIntoDaoException() {
        try (MockedStatic<ConnectionManager> mockedStatic = Mockito.mockStatic(ConnectionManager.class)) {
            mockedStatic.when(ConnectionManager::getConnection)
                    .thenThrow(new SQLException("Test SQL error"));

            assertThrows(DaoException.class,
                    () -> shoppingCartDao.addProduct(userId1, productId1, 1),
                    "When SQLException is thrown, a DaoException must be thrown."
            );
        }
    }

    @Test
    void removeProduct_checkThatProductWasRemovedFromShoppingCart() {
        shoppingCartDao.addProduct(userId1, productId1, 1);
        shoppingCartDao.removeProduct(userId1, productId1);
        List<ShoppingCart> productsList = shoppingCartDao.findByUserId(userId1);
        assertTrue(productsList.isEmpty(), "After removing product, shopping cart must be empty");
    }

    @Test
    void removeProduct_checkThatThrowDaoException_whenProductNotFound() {
        assertThrows(DaoException.class,
                () -> shoppingCartDao.removeProduct(userId1, productId1),
                "If product does not exist in shopping cart, DaoException must be thrown"
        );
    }

    @Test
    void removeProduct_checkThatWrapSqlExceptionIntoDaoException() {
        try (MockedStatic<ConnectionManager> mockedStatic = Mockito.mockStatic(ConnectionManager.class)) {
            mockedStatic.when(ConnectionManager::getConnection)
                    .thenThrow(new SQLException("Test SQL error"));

            assertThrows(DaoException.class,
                    () -> shoppingCartDao.removeProduct(userId1, productId1),
                    "When SQLException is thrown, DaoException must be thrown"
            );
        }
    }

    @Test
    void findByUserId_checkThatReturnOnlyProductsOfThisUser() {
        shoppingCartDao.addProduct(userId1, productId1, 1);
        shoppingCartDao.addProduct(userId1, productId2, 1);
        shoppingCartDao.addProduct(userId2, productId1, 2);

        List<ShoppingCart> user1Products = shoppingCartDao.findByUserId(userId1);

        assertEquals(2, user1Products.size(),
                "User1 must have 2 products in shopping cart"
        );

        boolean hasProduct1 = user1Products.stream()
                .anyMatch(p -> p.getProductId().equals(productId1)
                        && p.getQuantity() == 1);

        boolean hasProduct2 = user1Products.stream()
                .anyMatch(p -> p.getProductId().equals(productId2)
                        && p.getQuantity() == 1);

        assertTrue(hasProduct1, "User1 shopping cart must contain productId1");
        assertTrue(hasProduct2, "User1 shopping cart must contain productId2");

        List<ShoppingCart> user2Products = shoppingCartDao.findByUserId(userId2);
        assertEquals(1, user2Products.size(), "User2 must have 1 product in shopping cart");
        assertEquals(userId2, user2Products.getFirst().getUserId());
    }

    @Test
    void findByUserId_checkThatWrapSqlExceptionIntoDaoException() {
        try (MockedStatic<ConnectionManager> mockedStatic = Mockito.mockStatic(ConnectionManager.class)) {
            mockedStatic.when(ConnectionManager::getConnection)
                    .thenThrow(new SQLException("Test SQL error"));

            assertThrows(DaoException.class,
                    () -> shoppingCartDao.findByUserId(userId1),
                    "When SQLException is thrown, DaoException must be thrown"
            );
        }
    }

    @Test
    void clearCart_checkThatRemoveOnlyProductsOfThisUser() {
        shoppingCartDao.addProduct(userId1, productId1, 1);
        shoppingCartDao.addProduct(userId1, productId2, 1);
        shoppingCartDao.addProduct(userId2, productId1, 2);
        shoppingCartDao.clearCart(userId1);
        List<ShoppingCart> user1Products = shoppingCartDao.findByUserId(userId1);
        assertTrue(user1Products.isEmpty(), "After clearCart, user1 shopping cart must be empty");

        List<ShoppingCart> user2Products = shoppingCartDao.findByUserId(userId2);
        assertEquals(1, user2Products.size(), "clearCart must not remove product of user2");
    }

    @Test
    void clearCart_checkThatWrapSqlExceptionIntoDaoException() {
        try (MockedStatic<ConnectionManager> mockedStatic = Mockito.mockStatic(ConnectionManager.class)) {
            mockedStatic.when(ConnectionManager::getConnection)
                    .thenThrow(new SQLException("Test SQL error"));

            assertThrows(DaoException.class,
                    () -> shoppingCartDao.clearCart(userId1),
                    "When SQLException is thrown, DaoException must be thrown"
            );
        }
    }
}
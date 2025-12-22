package myshop.order.dao;

import myshop.common.db.ConnectionManager;
import myshop.common.exception.DaoException;
import myshop.order.model.Order;
import myshop.user.dao.JdbcUserDao;
import myshop.user.dao.UserDao;
import myshop.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JdbcOrderDaoTest {
    private OrderDao orderDao;
    private Long userId1;
    private Long userId2;

    @BeforeEach
    void setUp() throws SQLException {
        orderDao = new JdbcOrderDao();
        UserDao userDao = new JdbcUserDao();

        try (Connection connection = ConnectionManager.getConnection();
             Statement st = connection.createStatement()) {
            st.executeUpdate("DELETE FROM users");
            st.executeUpdate("DELETE FROM orders");
        }

        User user1 = new User();
        user1.setUsername("test_user1");
        user1.setEmail("testuser1@gmail.com");
        userId1 = userDao.createUser(user1).getId();

        User user2 = new User();
        user2.setUsername("test_user2");
        user2.setEmail("testuser2@gmail.com");
        userId2 = userDao.createUser(user2).getId();
    }

    @Test
    void saveOrder_checkThatOrderHasBeenSavedAndAssignedId() {
        Order order = new Order();
        order.setUserId(userId1);
        order.setOrderedProducts("product1, product2");
        order.setTotalAmount(new BigDecimal("1000.00"));
        Order savedOrder = orderDao.saveOrder(order);
        assertNotNull(savedOrder.getId(), "After saving, the order must have an ID.");
        assertEquals(userId1, savedOrder.getUserId());
        assertEquals("product1, product2", savedOrder.getOrderedProducts());
        assertEquals(new BigDecimal("1000.00"), savedOrder.getTotalAmount());

        List<Order> userOrders = orderDao.findAllOrdersByUserId(userId1);
        assertEquals(1, userOrders.size(), "The user must have 1 order");
        Order orderFromDb = userOrders.getFirst();
        assertEquals(savedOrder.getId(), orderFromDb.getId());
        assertEquals(savedOrder.getTotalAmount(), orderFromDb.getTotalAmount());
    }

    @Test
    void saveOrder_checkThatThrowIllegalArgumentExceptionWhenIdNotNull() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(userId1);
        order.setOrderedProducts("product1, product2");
        order.setTotalAmount(new BigDecimal("1000.00"));

        assertThrows(IllegalArgumentException.class,
                () -> orderDao.saveOrder(order),
                "When creating an order with an already defined id, " +
                        "there should be an IllegalArgumentException"
        );
    }

    @Test
    void saveOrder_checkThatThrowDaoExceptionWhenResultSetIsEmpty() throws SQLException {
        Order order = new Order();
        order.setUserId(userId1);
        order.setOrderedProducts("product1, product2");
        order.setTotalAmount(new BigDecimal("1000.00"));

        try (MockedStatic<ConnectionManager> mockedStatic = Mockito.mockStatic(ConnectionManager.class)) {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            ResultSet mockRs = mock(ResultSet.class);
            mockedStatic.when(ConnectionManager::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPs);
            when(mockPs.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(false);

            assertThrows(DaoException.class,
                    () -> orderDao.saveOrder(order),
                    "If ResultSet is empty, DaoException must be thrown."
            );
        }
    }

    @Test
    void saveOrder_checkThatWrapSqlExceptionIntoDaoException() {
        Order order = new Order();
        order.setUserId(userId1);
        order.setOrderedProducts("product1, product2");
        order.setTotalAmount(new BigDecimal("1000.00"));

        try (MockedStatic<ConnectionManager> mockedStatic = Mockito.mockStatic(ConnectionManager.class)) {
            mockedStatic.when(ConnectionManager::getConnection)
                    .thenThrow(new SQLException("Test SQL error"));

            assertThrows(DaoException.class,
                    () -> orderDao.saveOrder(order),
                    "When SQLException is thrown, DaoException must be thrown."
            );
        }
    }

    @Test
    void findAllOrdersByUserId_checkThatWrapSqlExceptionIntoDaoException() {
        try (MockedStatic<ConnectionManager> mockedStatic = Mockito.mockStatic(ConnectionManager.class)) {
            mockedStatic.when(ConnectionManager::getConnection)
                    .thenThrow(new SQLException("Test SQL error"));

            assertThrows(DaoException.class,
                    () -> orderDao.findAllOrdersByUserId(userId1),
                    "When SQLException is thrown, DaoException must be thrown."
            );
        }
    }

    @Test
    void findAllOrders_checkThatReturnAllOrders() {
        orderDao.saveOrder(new Order(userId1, "product1", new BigDecimal("10.00")));
        orderDao.saveOrder(new Order(userId1, "product2", new BigDecimal("20.00")));
        orderDao.saveOrder(new Order(userId2, "product3", new BigDecimal("30.00")));

        List<Order> orders = orderDao.findAllOrders();
        assertEquals(3, orders.size(), "3 orders should be returned");
    }

    @Test
    void findAllOrders_checkThatWrapSqlExceptionIntoDaoException() {
        try (MockedStatic<ConnectionManager> mockedStatic = Mockito.mockStatic(ConnectionManager.class)) {
            mockedStatic.when(ConnectionManager::getConnection)
                    .thenThrow(new SQLException("Test SQL error"));

            assertThrows(DaoException.class,
                    orderDao::findAllOrders,
                    "When SQLException is thrown, DaoException must be thrown.");
        }
    }
}
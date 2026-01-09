package myshop.order.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import myshop.common.exception.DaoException;
import myshop.common.jpa.JpaUtil;
import myshop.order.model.Order;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HibernateOrderDaoTest {
    @Mock
    private EntityManager em;

    @Mock
    private EntityTransaction transaction;

    @Mock
    private TypedQuery<Order> typedQuery;

    private OrderDao orderDao;

    private MockedStatic<JpaUtil> jpaUtilMock;

    @BeforeEach
    void setUp() {
        orderDao = new HibernateOrderDao();
        jpaUtilMock = Mockito.mockStatic(JpaUtil.class);
        jpaUtilMock.when(JpaUtil::getEntityManager).thenReturn(em);
        when(em.getTransaction()).thenReturn(transaction);
    }

    @AfterEach
    void tearDown() {
        jpaUtilMock.close();
    }

    @Test
    void saveOrder_checkThatPersistAndCommitWhenIdIsNull() {
        Order expected = new Order(
                null,
                1L,
                "product1, product2",
                new BigDecimal("100.00")
        );

        Order result = orderDao.saveOrder(expected);

        assertSame(expected, result);
        verify(transaction).begin();
        verify(em).persist(expected);
        verify(transaction).commit();
        verify(em).close();
    }

    @Test
    void saveOrder_checkThatThrowIllegalArgumentExceptionWhenIdNotNull() {
        Order order = new Order(
                10L,
                1L,
                "product1, product2",
                new BigDecimal("100.00")
        );

        assertThrows(IllegalArgumentException.class,
                () -> orderDao.saveOrder(order)
        );

        verifyNoInteractions(em);
    }

    @Test
    void saveOrder_checkThatRollbackAndThrowDaoExceptionWhenPersistFails() {
        Order order = new Order(
                null,
                1L,
                "product1, product2",
                new BigDecimal("100.00")
        );

        doThrow(new RuntimeException("DB error"))
                .when(em).persist(order);
        when(transaction.isActive()).thenReturn(true);

        assertThrows(DaoException.class,
                () -> orderDao.saveOrder(order)
        );

        verify(transaction).begin();
        verify(transaction).rollback();
        verify(em).close();
    }

    @Test
    void findAllOrdersByUserId_checkThatReturnOrdersWhenUserIdValid() {
        Long userId = 1L;
        List<Order> expected = List.of(
                new Order(1L, userId, "product1", new BigDecimal("100.00")),
                new Order(2L, userId, "product2", new BigDecimal("200.00"))
        );

        when(em.createQuery("SELECT o FROM Order o WHERE o.userId = :userId", Order.class))
                .thenReturn(typedQuery);
        when(typedQuery.setParameter("userId", userId)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expected);

        List<Order> result = orderDao.findAllOrdersByUserId(userId);

        assertEquals(expected, result);
        verify(em).close();
    }

    @Test
    void findAllOrdersByUserId_checkThatThrowIllegalArgumentExceptionWhenUserIdIsNull() {
        Long userId = null;

        assertThrows(IllegalArgumentException.class,
                () -> orderDao.findAllOrdersByUserId(userId)
        );

        verifyNoInteractions(em);
    }

    @Test
    void findAllOrdersByUserId_checkThatThrowDaoExceptionWhenQueryFails() {
        Long userId = 1L;

        when(em.createQuery("SELECT o FROM Order o WHERE o.userId = :userId", Order.class))
                .thenThrow(new RuntimeException("DB error"));

        assertThrows(DaoException.class,
                () -> orderDao.findAllOrdersByUserId(userId)
        );

        verify(em).close();
    }

    @Test
    void findAllOrders_checkThatReturnOrders() {
        List<Order> expected = List.of(
                new Order(1L, 1L, "product1", new BigDecimal("100.00")),
                new Order(2L, 2L, "product2", new BigDecimal("200.00"))
        );

        when(em.createQuery("SELECT o FROM Order o", Order.class))
                .thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expected);

        List<Order> result = orderDao.findAllOrders();

        assertEquals(expected, result);
        verify(em).close();
    }

    @Test
    void findAllOrders_checkThatThrowDaoExceptionWhenQueryFails() {
        when(em.createQuery("SELECT o FROM Order o", Order.class))
                .thenThrow(new RuntimeException("DB error"));

        assertThrows(DaoException.class,
                () -> orderDao.findAllOrders()
        );

        verify(em).close();
    }
}
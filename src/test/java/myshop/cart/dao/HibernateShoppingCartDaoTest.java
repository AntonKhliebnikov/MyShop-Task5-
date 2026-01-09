package myshop.cart.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import myshop.cart.model.ShoppingCart;
import myshop.cart.model.ShoppingCartId;
import myshop.common.exception.DaoException;
import myshop.common.jpa.JpaUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HibernateShoppingCartDaoTest {
    @Mock
    private EntityManager em;

    @Mock
    private EntityTransaction transaction;

    @Mock
    private TypedQuery<ShoppingCart> typedQuery;

    @Mock
    private Query query;

    private ShoppingCartDao shoppingCartDao;

    private MockedStatic<JpaUtil> jpaUtilMock;


    @BeforeEach
    void setUp() {
        shoppingCartDao = new HibernateShoppingCartDao();
        jpaUtilMock = Mockito.mockStatic(JpaUtil.class);
        jpaUtilMock.when(JpaUtil::getEntityManager).thenReturn(em);
        when(em.getTransaction()).thenReturn(transaction);
    }

    @AfterEach
    void tearDown() {
        jpaUtilMock.close();
    }

    @Test
    void addProduct_checkThatPersistNewProductWhenNotExists() {
        Long userId = 1L;
        Long productId = 2L;
        Integer quantity = 3;

        ShoppingCartId id = new ShoppingCartId(userId, productId);

        when(em.find(ShoppingCart.class, id)).thenReturn(null);

        shoppingCartDao.addProduct(userId, productId, quantity);

        verify(transaction).begin();
        verify(transaction).commit();
        verify(em).close();

        ArgumentCaptor<ShoppingCart> captor = ArgumentCaptor.forClass(ShoppingCart.class);
        verify(em).persist(captor.capture());
        ShoppingCart saved = captor.getValue();

        assertEquals(userId, saved.getUserId());
        assertEquals(productId, saved.getProductId());
        assertEquals(quantity, saved.getQuantity());
    }

    @Test
    void addProduct_checkThatUpdateQuantityWhenProductExists() {
        Long userId = 1L;
        Long productId = 2L;
        Integer quantityToAdd = 2;
        ShoppingCartId id = new ShoppingCartId(userId, productId);

        ShoppingCart existing = new ShoppingCart(userId, productId, 5);

        when(em.find(ShoppingCart.class, id)).thenReturn(existing);

        shoppingCartDao.addProduct(userId, productId, quantityToAdd);

        assertEquals(7, existing.getQuantity());

        verify(transaction).begin();
        verify(em).merge(existing);
        verify(transaction).commit();
        verify(em).close();

        verify(em, never()).persist(any(ShoppingCart.class));
    }

    @Test
    void addProduct_checkThatRollbackAndThrowDaoExceptionWhenError() {
        Long userId = 1L;
        Long productId = 2L;
        Integer quantity = 3;
        ShoppingCartId id = new ShoppingCartId(userId, productId);

        when(em.find(ShoppingCart.class, id))
                .thenThrow(new RuntimeException("DB error"));
        when(transaction.isActive()).thenReturn(true);

        assertThrows(DaoException.class,
                () -> shoppingCartDao.addProduct(userId, productId, quantity)
        );

        verify(transaction).begin();
        verify(transaction).rollback();
        verify(em).close();
    }

    @Test
    void addProduct_checkThatThrowIllegalArgumentExceptionWhenUserIdIsNull() {
        Long userId = null;
        Long productId = 1L;
        Integer quantity = 1;

        assertThrows(IllegalArgumentException.class,
                () -> shoppingCartDao.addProduct(userId, productId, quantity)
        );

        verifyNoInteractions(em);
    }

    @Test
    void addProduct_checkThatThrowIllegalArgumentExceptionWhenProductIdIsNull() {
        Long userId = 1L;
        Long productId = null;
        Integer quantity = 1;

        assertThrows(IllegalArgumentException.class,
                () -> shoppingCartDao.addProduct(userId, productId, quantity)
        );

        verifyNoInteractions(em);
    }

    @Test
    void addProduct_checkThatThrowIllegalArgumentExceptionWhenQuantityIsNull() {
        Long userId = 1L;
        Long productId = 2L;
        Integer quantity = null;

        assertThrows(IllegalArgumentException.class,
                () -> shoppingCartDao.addProduct(userId, productId, quantity)
        );

        verifyNoInteractions(em);
    }

    @Test
    void addProduct_checkThatThrowIllegalArgumentExceptionWhenQuantityIsZeroOrNegative() {
        Long userId = 1L;
        Long productId = 2L;

        assertThrows(IllegalArgumentException.class,
                () -> shoppingCartDao.addProduct(userId, productId, 0)
        );

        assertThrows(IllegalArgumentException.class,
                () -> shoppingCartDao.addProduct(userId, productId, -5)
        );

        verifyNoInteractions(em);
    }

    @Test
    void removeProduct_checkThatRemoveAndCommitWhenProductFound() {
        Long userId = 1L;
        Long productId = 2L;
        ShoppingCartId id = new ShoppingCartId(userId, productId);

        ShoppingCart item = new ShoppingCart(userId, productId, 5);
        when(em.find(ShoppingCart.class, id)).thenReturn(item);

        shoppingCartDao.removeProduct(userId, productId);

        verify(transaction).begin();
        verify(em).remove(item);
        verify(transaction).commit();
        verify(em).close();
    }

    @Test
    void removeProduct_checkThatThrowDaoExceptionWhenProductNotFound() {
        Long userId = 1L;
        Long productId = 2L;
        ShoppingCartId id = new ShoppingCartId(userId, productId);

        when(em.find(ShoppingCart.class, id)).thenReturn(null);
        when(transaction.isActive()).thenReturn(true);

        assertThrows(DaoException.class,
                () -> shoppingCartDao.removeProduct(userId, productId)
        );

        verify(transaction).begin();
        verify(transaction).rollback();
        verify(em).close();
    }

    @Test
    void removeProduct_checkThatRollbackAndThrowDaoExceptionWhenRemoveFails() {
        Long userId = 1L;
        Long productId = 2L;
        ShoppingCartId id = new ShoppingCartId(userId, productId);

        ShoppingCart item = new ShoppingCart(userId, productId, 5);

        when(em.find(ShoppingCart.class, id)).thenReturn(item);
        doThrow(new RuntimeException("DB error"))
                .when(em).remove(item);
        when(transaction.isActive()).thenReturn(true);

        assertThrows(DaoException.class,
                () -> shoppingCartDao.removeProduct(userId, productId)
        );

        verify(transaction).begin();
        verify(transaction).rollback();
        verify(em).close();
    }

    @Test
    void removeProduct_checkThatThrowIllegalArgumentExceptionWhenUserIdIsNull() {
        Long userId = null;
        Long productId = 1L;

        assertThrows(IllegalArgumentException.class,
                () -> shoppingCartDao.removeProduct(userId, productId)
        );

        verifyNoInteractions(em);
    }

    @Test
    void removeProduct_checkThatThrowIllegalArgumentExceptionWhenProductIdIsNull() {
        Long userId = 1L;
        Long productId = null;

        assertThrows(IllegalArgumentException.class,
                () -> shoppingCartDao.removeProduct(userId, productId)
        );

        verifyNoInteractions(em);
    }

    @Test
    void findByUserId_checkThatReturnCartByUserId() {
        Long userId = 1L;

        List<ShoppingCart> expected = List.of(
                new ShoppingCart(userId, 10L, 2),
                new ShoppingCart(userId, 20L, 1)
        );

        when(em.createQuery("SELECT sc FROM ShoppingCart sc WHERE sc.userId = :userId",
                ShoppingCart.class)).thenReturn(typedQuery);
        when(typedQuery.setParameter("userId", userId)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expected);

        List<ShoppingCart> result = shoppingCartDao.findByUserId(userId);

        assertEquals(expected, result);
        verify(em).close();
    }

    @Test
    void findByUserId_checkThatThrowDaoExceptionWhenQueryFails() {
        Long userId = 1L;

        when(em.createQuery("SELECT sc FROM ShoppingCart sc WHERE sc.userId = :userId",
                ShoppingCart.class)).thenThrow(new RuntimeException("DB error"));

        assertThrows(DaoException.class,
                () -> shoppingCartDao.findByUserId(userId)
        );

        verify(em).close();
    }

    @Test
    void findByUserId_checkThatThrowIllegalArgumentExceptionWhenUserIdIsNull() {
        Long userId = null;

        assertThrows(IllegalArgumentException.class,
                () -> shoppingCartDao.findByUserId(userId)
        );

        verifyNoInteractions(em);
    }

    @Test
    void clearCart_checkThatDeleteAndCommitCartByUserId() {
        Long userId = 1L;

        when(em.createQuery("DELETE FROM ShoppingCart sc WHERE sc.userId = :userId"))
                .thenReturn(query);
        when(query.setParameter("userId", userId)).thenReturn(query);
        when(query.executeUpdate()).thenReturn(3);

        shoppingCartDao.clearCart(userId);

        verify(transaction).begin();
        verify(query).executeUpdate();
        verify(transaction).commit();
        verify(em).close();
    }

    @Test
    void clearCart_checkThatRollbackAndThrowDaoExceptionWhenError() {
        Long userId = 1L;

        when(em.createQuery("DELETE FROM ShoppingCart sc WHERE sc.userId = :userId"))
                .thenThrow(new RuntimeException("DB error"));
        when(transaction.isActive()).thenReturn(true);

        assertThrows(DaoException.class,
                () -> shoppingCartDao.clearCart(userId)
        );

        verify(transaction).begin();
        verify(transaction).rollback();
        verify(em).close();
    }

    @Test
    void clearCart_checkThatThrowIllegalArgumentExceptionWhenUserIdIsNull() {
        Long userId = null;

        assertThrows(IllegalArgumentException.class,
                () -> shoppingCartDao.clearCart(userId)
        );

        verifyNoInteractions(em);
    }
}
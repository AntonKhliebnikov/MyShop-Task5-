package myshop.product.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import myshop.common.exception.DaoException;
import myshop.common.jpa.JpaUtil;
import myshop.product.model.Product;
import myshop.user.model.User;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HibernateProductDaoTest {
    @Mock
    private EntityManager em;

    @Mock
    private EntityTransaction transaction;

    @Mock
    private TypedQuery<Product> typedQuery;

    private ProductDao productDao;

    private MockedStatic<JpaUtil> jpaUtilMock;

    @BeforeEach
    void setUp() {
        productDao = new HibernateProductDao();
        jpaUtilMock = Mockito.mockStatic(JpaUtil.class);
        jpaUtilMock.when(JpaUtil::getEntityManager).thenReturn(em);
        when(em.getTransaction()).thenReturn(transaction);
    }

    @AfterEach
    void tearDown() {
        jpaUtilMock.close();
    }

    @Test
    void createProduct_CheckThatPersistAndCommitWhenIdIsNull() {
        Product expected = new Product(null, "product", new BigDecimal("100.00"));
        Product result = productDao.createProduct(expected);

        assertSame(expected, result);
        verify(transaction).begin();
        verify(em).persist(expected);
        verify(transaction).commit();
        verify(em).close();
    }

    @Test
    void createProduct_checkThatThrowIllegalArgumentExceptionWhenIdNotNull() {
        Product product = new Product(1L, "product", new BigDecimal("100.00"));

        assertThrows(IllegalArgumentException.class,
                () -> productDao.createProduct(product)
        );

        verifyNoInteractions(em);
    }

    @Test
    void createProduct_checkThatRollbackAndThrowDaoExceptionWhenPersistFails() {
        Product product = new Product(null, "product", new BigDecimal("100.00"));

        doThrow(new RuntimeException("DB error")).when(em).persist(product);
        when(transaction.isActive()).thenReturn(true);

        assertThrows(DaoException.class, () -> productDao.createProduct(product));
        verify(transaction).begin();
        verify(transaction).rollback();
        verify(em).close();
    }

    @Test
    void findAllProducts_checkThatReturnListProducts() {
        List<Product> expected = List.of(
                new Product(1L, "product1", new BigDecimal("100.00")),
                new Product(2L, "product2", new BigDecimal("200.00"))
        );

        when(em.createQuery("SELECT p FROM Product p", Product.class)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expected);

        List<Product> result = productDao.findAllProducts();

        assertEquals(expected, result);
        verify(em).close();
    }

    @Test
    void findAllProducts_checkThatThrowDaoExceptionWhenQueryFails() {
        when(em.createQuery("SELECT p FROM Product p", Product.class))
                .thenThrow(new RuntimeException("DB error"));

        assertThrows(DaoException.class,
                () -> productDao.findAllProducts()
        );

        verify(em).close();
    }

    @Test
    void findById_checkThatReturnProductWhenFoundById() {
        Long id = 1L;
        Product product = new Product(id, "product", new BigDecimal("100.00"));

        when(em.find(Product.class, id)).thenReturn(product);

        Product result = productDao.findById(id);

        assertSame(product, result);
        verify(em).close();
    }

    @Test
    void findById_checkThatThrowDaoExceptionWhenProductNotFound() {
        Long id = 1L;
        when(em.find(Product.class, id)).thenReturn(null);

        assertThrows(DaoException.class,
                () -> productDao.findById(id)
        );

        verify(em).close();
    }

    @Test
    void findById_checkThatThrowDaoExceptionWhenFindFails() {
        Long id = 1L;
        when(em.find(Product.class, id))
                .thenThrow(new RuntimeException("DB error"));

        assertThrows(DaoException.class,
                () -> productDao.findById(id)
        );

        verify(em).close();
    }

    @Test
    void updateProduct_checkThatMergeAndCommitWhenIdNotNull() {
        Product product = new Product(1L, "product", new BigDecimal("100.00"));

        productDao.updateProduct(product);

        verify(transaction).begin();
        verify(em).merge(product);
        verify(transaction).commit();
        verify(em).close();
    }

    @Test
    void updateProduct_checkThatThrowIllegalArgumentExceptionWhenIdIsNull() {
        Product product = new Product(null, "product", new BigDecimal("100.00"));

        assertThrows(IllegalArgumentException.class,
                () -> productDao.updateProduct(product)
        );

        verifyNoInteractions(em);
    }

    @Test
    void updateProduct_checkThatRollbackAndThrowDaoExceptionWhenMergeFails() {
        Product product = new Product(1L, "product", new BigDecimal("100.00"));

        doThrow(new RuntimeException("DB error"))
                .when(em).merge(product);

        when(transaction.isActive()).thenReturn(true);

        assertThrows(DaoException.class,
                () -> productDao.updateProduct(product));

        verify(transaction).rollback();
        verify(em).close();
    }

    @Test
    void deleteById_checkThatRemoveAndCommitWhenProductFound() {
        Long id = 1L;
        Product product = new Product(id, "product", new BigDecimal("100.00"));
        when(em.find(Product.class, id)).thenReturn(product);

        productDao.deleteById(id);

        verify(transaction).begin();
        verify(em).remove(product);
        verify(transaction).commit();
        verify(em).close();
    }

    @Test
    void deleteById_checkThatThrowDaoExceptionWhenProductNotFound() {
        Long id = 1L;
        when(em.find(Product.class, id)).thenReturn(null);
        when(transaction.isActive()).thenReturn(true);

        assertThrows(DaoException.class,
                () -> productDao.deleteById(id)
        );

        verify(transaction).rollback();
        verify(em).close();
    }

    @Test
    void deleteById_checkThatRollbackAndThrowDaoExceptionWhenRemoveFails() {
        Long id = 1L;
        Product product = new Product(id, "product", new BigDecimal("100.00"));

        when(em.find(Product.class, id)).thenReturn(product);
        doThrow(new RuntimeException("DB error"))
                .when(em).remove(product);
        when(transaction.isActive()).thenReturn(true);

        assertThrows(DaoException.class,
                () -> productDao.deleteById(id)
        );

        verify(transaction).rollback();
        verify(em).close();
    }
}
package myshop.user.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import myshop.common.exception.DaoException;
import myshop.common.jpa.JpaUtil;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HibernateUserDaoTest {
    @Mock
    private EntityManager em;

    @Mock
    private EntityTransaction transaction;

    @Mock
    private TypedQuery<User> typedQuery;

    private UserDao userDao;

    private MockedStatic<JpaUtil> jpaUtilMock;

    @BeforeEach
    void setUp() {
        userDao = new HibernateUserDao();
        jpaUtilMock = Mockito.mockStatic(JpaUtil.class);
        jpaUtilMock.when(JpaUtil::getEntityManager).thenReturn(em);
        when(em.getTransaction()).thenReturn(transaction);
    }

    @AfterEach
    void tearDown() {
        jpaUtilMock.close();
    }

    @Test
    void createUser_CheckThatPersistAndCommitWhenIdIsNull() {
        User expected = new User(null, "testUser", "testUser@gmail.com");
        User result = userDao.createUser(expected);

        assertSame(expected, result);
        verify(transaction).begin();
        verify(em).persist(expected);
        verify(transaction).commit();
        verify(em).close();
    }

    @Test
    void createUser_checkThatThrowIllegalArgumentExceptionWhenIdNotNull() {
        User user = new User(1L, "testUser", "testUser@gmail.com");

        assertThrows(IllegalArgumentException.class,
                () -> userDao.createUser(user)
        );

        verifyNoInteractions(em);
    }

    @Test
    void createUser_checkThatRollbackAndThrowDaoExceptionWhenPersistFails() {
        User user = new User(null, "testUser", "testUser@gmail.com");

        doThrow(new RuntimeException("DB error")).when(em).persist(user);
        when(transaction.isActive()).thenReturn(true);

        assertThrows(DaoException.class, () -> userDao.createUser(user));
        verify(transaction).begin();
        verify(transaction).rollback();
        verify(em).close();
    }

    @Test
    void findAllUsers_checkThatReturnListUsers() {
        List<User> expected = List.of(
                new User(1L, "user1", "user1@gmail.com"),
                new User(2L, "user2", "user2@gmail.com")
        );

        when(em.createQuery("SELECT u FROM User u", User.class)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expected);

        List<User> result = userDao.findAllUsers();

        assertEquals(expected, result);
        verify(em).close();
    }

    @Test
    void findAllUsers_checkThatThrowDaoExceptionWhenQueryFails() {
        when(em.createQuery("SELECT u FROM User u", User.class))
                .thenThrow(new RuntimeException("DB error"));

        assertThrows(DaoException.class,
                () -> userDao.findAllUsers()
        );

        verify(em).close();
    }

    @Test
    void findById_checkThatReturnUserWhenFoundById() {
        Long id = 1L;
        User user = new User(id, "testUser", "testUser@gmail.com");

        when(em.find(User.class, id)).thenReturn(user);

        User result = userDao.findById(id);

        assertSame(user, result);
        verify(em).close();
    }

    @Test
    void findById_checkThatThrowDaoExceptionWhenUserNotFound() {
        Long id = 1L;
        when(em.find(User.class, id)).thenReturn(null);

        assertThrows(DaoException.class,
                () -> userDao.findById(id)
        );

        verify(em).close();
    }

    @Test
    void findById_checkThatThrowDaoExceptionWhenFindFails() {
        Long id = 1L;
        when(em.find(User.class, id))
                .thenThrow(new RuntimeException("DB error"));

        assertThrows(DaoException.class,
                () -> userDao.findById(id)
        );

        verify(em).close();
    }

    @Test
    void updateUser_checkThatMergeAndCommitWhenIdNotNull() {
        User user = new User(1L, "testUser", "testUser@gmail.com");

        userDao.updateUser(user);

        verify(transaction).begin();
        verify(em).merge(user);
        verify(transaction).commit();
        verify(em).close();
    }

    @Test
    void updateUser_checkThatThrowIllegalArgumentExceptionWhenIdNull() {
        User user = new User(null, "john", "john@example.com");

        assertThrows(IllegalArgumentException.class,
                () -> userDao.updateUser(user)
        );

        verifyNoInteractions(em);
    }

    @Test
    void updateUser_checkThatRollbackAndThrowDaoExceptionWhenMergeFails() {
        User user = new User(1L, "testUser", "testUser@gmail.com");

        doThrow(new RuntimeException("DB error"))
                .when(em).merge(user);

        when(transaction.isActive()).thenReturn(true);

        assertThrows(DaoException.class,
                () -> userDao.updateUser(user));

        verify(transaction).rollback();
        verify(em).close();
    }

    @Test
    void deleteById_checkThatRemoveAndCommitWhenUserFound() {
        Long id = 1L;
        User user = new User(id, "testUser", "testUser@gmail.com");
        when(em.find(User.class, id)).thenReturn(user);

        userDao.deleteById(id);

        verify(transaction).begin();
        verify(em).remove(user);
        verify(transaction).commit();
        verify(em).close();
    }

    @Test
    void deleteById_checkThatThrowDaoExceptionWhenUserNotFound() {
        Long id = 1L;
        when(em.find(User.class, id)).thenReturn(null);
        when(transaction.isActive()).thenReturn(true);

        assertThrows(DaoException.class,
                () -> userDao.deleteById(id)
        );

        verify(transaction).rollback();
        verify(em).close();
    }

    @Test
    void deleteById_checkThatRollbackAndThrowDaoExceptionWhenRemoveFails() {
        Long id = 1L;
        User user = new User(id, "john", "john@example.com");

        when(em.find(User.class, id)).thenReturn(user);
        doThrow(new RuntimeException("DB error"))
                .when(em).remove(user);
        when(transaction.isActive()).thenReturn(true);

        assertThrows(DaoException.class,
                () -> userDao.deleteById(id)
        );

        verify(transaction).rollback();
        verify(em).close();
    }
}
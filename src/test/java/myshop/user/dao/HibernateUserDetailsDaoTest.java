package myshop.user.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import myshop.common.exception.DaoException;
import myshop.common.jpa.JpaUtil;
import myshop.user.model.User;
import myshop.user.model.UserDetails;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HibernateUserDetailsDaoTest {
    @Mock
    private EntityManager em;

    @Mock
    private EntityTransaction transaction;

    @Mock
    private TypedQuery<UserDetails> typedQuery;

    private UserDetailsDao userDetailsDao;

    private MockedStatic<JpaUtil> jpaUtilMock;

    @BeforeEach
    void setUp() {
        userDetailsDao = new HibernateUserDetailsDao();
        jpaUtilMock = Mockito.mockStatic(JpaUtil.class);
        jpaUtilMock.when(JpaUtil::getEntityManager).thenReturn(em);
        when(em.getTransaction()).thenReturn(transaction);
    }

    @AfterEach
    void tearDown() {
        jpaUtilMock.close();
    }

    @Test
    void createUserDetails_CheckThatPersistAndCommitWhenUserIdNotNull() {
        UserDetails userDetails = new UserDetails(1L,
                "firstName",
                "lastName",
                "address",
                "phone");

        userDetailsDao.createUserDetails(userDetails);

        verify(transaction).begin();
        verify(em).persist(userDetails);
        verify(transaction).commit();
        verify(em).close();
    }

    @Test
    void createUserDetails_checkThatThrowIllegalArgumentExceptionWhenUserIdIsNull() {
        UserDetails userDetails = new UserDetails(null,
                "firstName",
                "lastName",
                "address",
                "phone");

        assertThrows(IllegalArgumentException.class,
                () -> userDetailsDao.createUserDetails(userDetails)
        );

        verifyNoInteractions(em);
    }

    @Test
    void createUserDetails_checkThatRollbackAndThrowDaoExceptionWhenPersistFails() {
        UserDetails userDetails = new UserDetails(1L,
                "firstName",
                "lastName",
                "address",
                "phone");

        doThrow(new RuntimeException("DB error")).when(em).persist(userDetails);
        when(transaction.isActive()).thenReturn(true);

        assertThrows(DaoException.class,
                () -> userDetailsDao.createUserDetails(userDetails)
        );

        verify(transaction).begin();
        verify(transaction).rollback();
        verify(em).close();
    }

    @Test
    void findAllUserDetails_checkThatReturnListUserDetails() {
        List<UserDetails> expected = List.of(
                new UserDetails(1L, "firstName1", "lastName1", "address1", "phone1"),
                new UserDetails(2L, "firstName2", "lastName2", "address2", "phone2")
        );

        when(em.createQuery("SELECT ud FROM UserDetails ud", UserDetails.class)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(expected);

        List<UserDetails> result = userDetailsDao.findAllUserDetails();

        assertEquals(expected, result);
        verify(em).close();
    }

    @Test
    void findAllUserDetails_checkThatThrowDaoExceptionWhenQueryFails() {
        when(em.createQuery("SELECT ud FROM UserDetails ud", UserDetails.class))
                .thenThrow(new RuntimeException("DB error"));

        assertThrows(DaoException.class,
                () -> userDetailsDao.findAllUserDetails()
        );

        verify(em).close();
    }

    @Test
    void findByUserId_checkThatReturnUserDetailsWhenFoundByUserId() {
        Long id = 1L;
        UserDetails userDetails = new UserDetails(id,
                "firstName",
                "lastName",
                "address",
                "phone");

        when(em.find(UserDetails.class, id)).thenReturn(userDetails);

        UserDetails result = userDetailsDao.findByUserId(id);

        assertSame(userDetails, result);
        verify(em).close();
    }

    @Test
    void findByUserId_checkThatThrowDaoExceptionWhenUserDetailsNotFound() {
        Long id = 1L;
        when(em.find(UserDetails.class, id)).thenReturn(null);

        assertThrows(DaoException.class,
                () -> userDetailsDao.findByUserId(id)
        );

        verify(em).close();
    }

    @Test
    void findByUserId_checkThatThrowDaoExceptionWhenFindFails() {
        Long id = 1L;
        when(em.find(UserDetails.class, id))
                .thenThrow(new RuntimeException("DB error"));

        assertThrows(DaoException.class,
                () -> userDetailsDao.findByUserId(id)
        );

        verify(em).close();
    }

    @Test
    void updateUserDetails_checkThatMergeAndCommitWhenUserIdNotNull() {
        UserDetails userDetails = new UserDetails(1L,
                "firstName",
                "lastName",
                "address",
                "phone");

        userDetailsDao.updateUserDetails(userDetails);

        verify(transaction).begin();
        verify(em).merge(userDetails);
        verify(transaction).commit();
        verify(em).close();
    }

    @Test
    void updateUserDetails_checkThatThrowIllegalArgumentExceptionWhenIdIsNull() {
        UserDetails userDetails = new UserDetails(null,
                "firstName",
                "lastName",
                "address",
                "phone");

        assertThrows(IllegalArgumentException.class,
                () -> userDetailsDao.updateUserDetails(userDetails)
        );

        verifyNoInteractions(em);
    }

    @Test
    void updateUserDetails_checkThatRollbackAndThrowDaoExceptionWhenMergeFails() {
        UserDetails userDetails = new UserDetails(1L,
                "firstName",
                "lastName",
                "address",
                "phone");

        doThrow(new RuntimeException("DB error"))
                .when(em).merge(userDetails);

        when(transaction.isActive()).thenReturn(true);

        assertThrows(DaoException.class,
                () -> userDetailsDao.updateUserDetails(userDetails));

        verify(transaction).rollback();
        verify(em).close();
    }

    @Test
    void deleteByUserId_checkThatRemoveAndCommitWhenUserDetailsFound() {
        Long id = 1L;
        UserDetails userDetails = new UserDetails(id,
                "firstName",
                "lastName",
                "address",
                "phone");
        when(em.find(UserDetails.class, id)).thenReturn(userDetails);

        userDetailsDao.deleteByUserId(id);

        verify(transaction).begin();
        verify(em).remove(userDetails);
        verify(transaction).commit();
        verify(em).close();
    }

    @Test
    void deleteByUserId_checkThatThrowDaoExceptionWhenUserDetailsNotFound() {
        Long id = 1L;
        when(em.find(UserDetails.class, id)).thenReturn(null);
        when(transaction.isActive()).thenReturn(true);

        assertThrows(DaoException.class,
                () -> userDetailsDao.deleteByUserId(id)
        );

        verify(transaction).rollback();
        verify(em).close();
    }

    @Test
    void deleteByUserId_checkThatRollbackAndThrowDaoExceptionWhenRemoveFails() {
        Long id = 1L;
        UserDetails userDetails = new UserDetails(id,
                "firstName",
                "lastName",
                "address",
                "phone");

        when(em.find(UserDetails.class, id)).thenReturn(userDetails);
        doThrow(new RuntimeException("DB error"))
                .when(em).remove(userDetails);
        when(transaction.isActive()).thenReturn(true);

        assertThrows(DaoException.class,
                () -> userDetailsDao.deleteByUserId(id)
        );

        verify(transaction).rollback();
        verify(em).close();
    }
}
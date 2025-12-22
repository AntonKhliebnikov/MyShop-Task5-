package myshop.user.dao;

import myshop.common.db.ConnectionManager;
import myshop.common.exception.DaoException;
import myshop.user.model.User;
import myshop.user.model.UserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class JdbcUserDetailsDaoTest {
    private UserDetailsDao userDetailsDao;
    private Long userId1;
    private Long userId2;

    @BeforeEach
    void setUp() throws SQLException {
        userDetailsDao = new JdbcUserDetailsDao();
        UserDao userDao = new JdbcUserDao();
        try (Connection connection = ConnectionManager.getConnection();
             Statement st = connection.createStatement()) {
            st.executeUpdate("DELETE FROM user_details");
            st.executeUpdate("DELETE FROM users");
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
    void createUserDetails_checkThatThrowIllegalArgumentExceptionWhenUserIdIsNull() {
        UserDetails userDetails = new UserDetails();
        userDetails.setFirstName("firstname");
        userDetails.setLastName("lastname");
        userDetails.setAddress("address");
        userDetails.setPhone("phone");

        assertThrows(IllegalArgumentException.class,
                () -> userDetailsDao.createUserDetails(userDetails),
                "При создании данных пользователя без userId должен быть IllegalArgumentException"
        );
    }

    @Test
    void createUserDetails_checkThatUserDetailsIsSaved() {
        UserDetails userDetails = new UserDetails();
        userDetails.setUserId(userId1);
        userDetails.setFirstName("firstname");
        userDetails.setLastName("lastname");
        userDetails.setAddress("address");
        userDetails.setPhone("phone");
        userDetailsDao.createUserDetails(userDetails);

        assertNotNull(userDetails.getUserId(), "После создания у данных пользователя должен быть userId");
        assertEquals(userId1, userDetails.getUserId());
        assertEquals("firstname", userDetails.getFirstName());
        assertEquals("lastname", userDetails.getLastName());
        assertEquals("address", userDetails.getAddress());
        assertEquals("phone", userDetails.getPhone());
    }

    @Test
    void createUserDetails_checkThatUserDetailsInDb() {
        UserDetails userDetails = new UserDetails();
        userDetails.setUserId(userId1);
        userDetails.setFirstName("firstname");
        userDetails.setLastName("lastname");
        userDetails.setAddress("address");
        userDetails.setPhone("phone");
        userDetailsDao.createUserDetails(userDetails);
        UserDetails userDetailsFromDb = userDetailsDao.findByUserId(userDetails.getUserId());

        assertNotNull(userDetailsFromDb, "Данные пользователя должны существовать в БД");
        assertEquals(userId1, userDetailsFromDb.getUserId());
        assertEquals("firstname", userDetailsFromDb.getFirstName());
        assertEquals("lastname", userDetailsFromDb.getLastName());
        assertEquals("address", userDetailsFromDb.getAddress());
        assertEquals("phone", userDetailsFromDb.getPhone());
    }

    @Test
    void createUserDetails_checkThatWrapSqlExceptionIntoDaoException() {
        UserDetails userDetails = new UserDetails();
        userDetails.setUserId(userId1);
        userDetails.setFirstName("firstname");
        userDetails.setLastName("lastname");
        userDetails.setAddress("address");
        userDetails.setPhone("phone");

        try (MockedStatic<ConnectionManager> mockedStatic = Mockito.mockStatic(ConnectionManager.class)) {
            mockedStatic.when(ConnectionManager::getConnection)
                    .thenThrow(new SQLException("Test SQL error"));

            assertThrows(DaoException.class,
                    () -> userDetailsDao.createUserDetails(userDetails),
                    "При SQLException должен быть брошен DaoException"
            );
        }
    }

    @Test
    void findAllUserDetails_checkThatReturnListUserDetails() {
        UserDetails userDetails1 = new UserDetails();
        userDetails1.setUserId(userId1);
        userDetails1.setFirstName("firstname1");
        userDetails1.setLastName("lastname1");
        userDetails1.setAddress("address1");
        userDetails1.setPhone("phone1");
        userDetailsDao.createUserDetails(userDetails1);

        UserDetails userDetails2 = new UserDetails();
        userDetails2.setUserId(userId2);
        userDetails2.setFirstName("firstname2");
        userDetails2.setLastName("lastname2");
        userDetails2.setAddress("address2");
        userDetails2.setPhone("phone2");
        userDetailsDao.createUserDetails(userDetails2);

        List<UserDetails> userDetailsList = userDetailsDao.findAllUserDetails();

        assertEquals(2, userDetailsList.size(), "Должны вернуться данные по 2 пользователям");
        boolean testUserDetails1 = userDetailsList.stream()
                .anyMatch(ud -> Objects.equals(ud.getUserId(), userId1)
                        && ud.getFirstName().equals("firstname1")
                        && ud.getLastName().equals("lastname1")
                        && ud.getAddress().equals("address1")
                        && ud.getPhone().equals("phone1")
                );

        boolean testUserDetails2 = userDetailsList.stream()
                .anyMatch(ud -> Objects.equals(ud.getUserId(), userId2)
                        && ud.getFirstName().equals("firstname2")
                        && ud.getLastName().equals("lastname2")
                        && ud.getAddress().equals("address2")
                        && ud.getPhone().equals("phone2")
                );

        assertTrue(testUserDetails1, "Среди данных пользователей должны быть данные testUserDetails1");
        assertTrue(testUserDetails2, "Среди данных пользователей должны быть данные testUserDetails2");
    }

    @Test
    void findAllUserDetails_checkThatWrapSqlExceptionIntoDaoException() {
        UserDetails userDetails1 = new UserDetails();
        userDetails1.setUserId(userId1);
        userDetails1.setFirstName("firstname1");
        userDetails1.setLastName("lastname1");
        userDetails1.setAddress("address1");
        userDetails1.setPhone("phone1");
        userDetailsDao.createUserDetails(userDetails1);

        UserDetails userDetails2 = new UserDetails();
        userDetails2.setUserId(userId2);
        userDetails2.setFirstName("firstname2");
        userDetails2.setLastName("lastname2");
        userDetails2.setAddress("address2");
        userDetails2.setPhone("phone2");
        userDetailsDao.createUserDetails(userDetails2);

        try (MockedStatic<ConnectionManager> mockedStatic = Mockito.mockStatic(ConnectionManager.class)) {
            mockedStatic.when(ConnectionManager::getConnection)
                    .thenThrow(new SQLException("Test SQL error"));

            assertThrows(DaoException.class,
                    () -> userDetailsDao.findAllUserDetails(),
                    "При SQLException должен быть брошен DaoException"
            );
        }
    }

    @Test
    void findByUserId_checkThatReturnNullWhenUserDetailsDoesNotExist() {
        Long notExistingUserId = Long.MAX_VALUE;
        UserDetails userDetailsByUserId = userDetailsDao.findByUserId(notExistingUserId);
        assertNull(userDetailsByUserId, "Если данных пользователя в БД нет, findByUserId должен вернуть null ");
    }

    @Test
    void findByUserId_checkThatWrapSqlExceptionIntoDaoException() {
        UserDetails userDetails = new UserDetails();
        userDetails.setUserId(userId1);
        userDetails.setFirstName("firstname");
        userDetails.setLastName("lastname");
        userDetails.setAddress("address");
        userDetails.setPhone("phone");
        userDetailsDao.createUserDetails(userDetails);

        try (MockedStatic<ConnectionManager> mockedStatic = Mockito.mockStatic(ConnectionManager.class)) {
            mockedStatic.when(ConnectionManager::getConnection)
                    .thenThrow(new SQLException("Test SQL error"));

            assertThrows(DaoException.class,
                    () -> userDetailsDao.findByUserId(userDetails.getUserId()),
                    "При SQLException должен быть брошен DaoException"
            );
        }
    }

    @Test
    void updateUserDetails_checkThatThrowIllegalArgumentExceptionWhenUserIdIsNull() {
        UserDetails userDetails = new UserDetails();
        userDetails.setFirstName("firstname");
        userDetails.setLastName("lastname");
        userDetails.setAddress("address");
        userDetails.setPhone("phone");

        assertThrows(IllegalArgumentException.class,
                () -> userDetailsDao.updateUserDetails(userDetails),
                "При обновлении данных пользователя без userId должен быть IllegalArgumentException"
        );
    }

    @Test
    void updateUserDetails_checkThatChangeUserDetailsData() {
        UserDetails userDetails = new UserDetails();
        userDetails.setUserId(userId1);
        userDetails.setFirstName("firstname");
        userDetails.setLastName("lastname");
        userDetails.setAddress("address");
        userDetails.setPhone("phone");
        userDetailsDao.createUserDetails(userDetails);

        userDetails.setFirstName("updated_firstname");
        userDetails.setLastName("updated_lastname");
        userDetails.setAddress("updated_address");
        userDetails.setPhone("updated_phone");
        userDetailsDao.updateUserDetails(userDetails);

        UserDetails userDetailsFromDb = userDetailsDao.findByUserId(userDetails.getUserId());

        assertNotNull(userDetailsFromDb, "После обновления данные пользователя должны существовать");
        assertEquals("updated_firstname", userDetailsFromDb.getFirstName());
        assertEquals("updated_lastname", userDetailsFromDb.getLastName());
        assertEquals("updated_address", userDetailsFromDb.getAddress());
        assertEquals("updated_phone", userDetailsFromDb.getPhone());
    }

    @Test
    void updateUserDetails_checkThatThrowDaoExceptionWhenUserDetailsNotFoundByUserId() {
        UserDetails userDetails = new UserDetails();
        userDetails.setUserId(Long.MAX_VALUE);
        userDetails.setFirstName("firstname");
        userDetails.setLastName("lastname");
        userDetails.setAddress("address");
        userDetails.setPhone("phone");

        assertThrows(DaoException.class,
                () -> userDetailsDao.updateUserDetails(userDetails),
                "Если данные пользователя не найдены по userId, должен быть DaoException"
        );
    }

    @Test
    void updateUserDetails_checkThatWrapSqlExceptionIntoDaoException() {
        UserDetails userDetails = new UserDetails();
        userDetails.setUserId(userId1);
        userDetails.setFirstName("firstname");
        userDetails.setLastName("lastname");
        userDetails.setAddress("address");
        userDetails.setPhone("phone");

        try (MockedStatic<ConnectionManager> mockedStatic = Mockito.mockStatic(ConnectionManager.class)) {
            mockedStatic.when(ConnectionManager::getConnection)
                    .thenThrow(new SQLException("Test SQL error"));

            assertThrows(DaoException.class,
                    () -> userDetailsDao.updateUserDetails(userDetails),
                    "При SQLException должен быть брошен DaoException"
            );
        }
    }

    @Test
    void deleteByUserId_checkThatUserDetailsRemoved() {
        UserDetails userDetails = new UserDetails();
        userDetails.setUserId(userId1);
        userDetails.setFirstName("firstname");
        userDetails.setLastName("lastname");
        userDetails.setAddress("address");
        userDetails.setPhone("phone");
        userDetailsDao.createUserDetails(userDetails);

        Long userId = userDetails.getUserId();
        userDetailsDao.deleteByUserId(userId);
        UserDetails deletedUserDetails = userDetailsDao.findByUserId(userId);

        assertNull(deletedUserDetails, "После удаления данные пользователя не должен существовать в БД");
    }

    @Test
    void deleteByUserId_checkThatThrowDaoExceptionWhenUserDetailsWasNotDeleteByUserId() {
        long notExistingUserId = Long.MAX_VALUE;

        assertThrows(DaoException.class,
                () -> userDetailsDao.deleteByUserId(notExistingUserId),
                "Если данные пользователя не найден по userId, должен быть DaoException"
        );
    }

    @Test
    void deleteByUserId_checkThatWrapSqlExceptionIntoDaoException() {
        UserDetails userDetails = new UserDetails();
        userDetails.setUserId(userId1);
        userDetails.setFirstName("firstname");
        userDetails.setLastName("lastname");
        userDetails.setAddress("address");
        userDetails.setPhone("phone");
        userDetailsDao.createUserDetails(userDetails);

        try (MockedStatic<ConnectionManager> mockedStatic = Mockito.mockStatic(ConnectionManager.class)) {
            mockedStatic.when(ConnectionManager::getConnection)
                    .thenThrow(new SQLException("Test SQL error"));

            assertThrows(DaoException.class,
                    () -> userDetailsDao.deleteByUserId(userDetails.getUserId()),
                    "При SQLException должен быть брошен DaoException"
            );
        }
    }
}
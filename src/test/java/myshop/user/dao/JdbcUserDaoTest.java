package myshop.user.dao;

import myshop.common.db.ConnectionManager;
import myshop.common.exception.DaoException;
import myshop.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class JdbcUserDaoTest {
    private UserDao userDao;

    @BeforeEach
    void setUp() throws SQLException {
        userDao = new JdbcUserDao();
        try (Connection connection = ConnectionManager.getConnection();
             Statement st = connection.createStatement()) {
            st.executeUpdate("DELETE FROM user_details");
            st.executeUpdate("DELETE FROM users");
        }
    }

    @Test
    void createUser_checkThatUserIsSavedAndAssignedAnId() {
        User newUser = new User();
        newUser.setUsername("test_user");
        newUser.setEmail("testuser@gmail.com");
        User createdUser = userDao.createUser(newUser);
        assertNotNull(createdUser.getId(), "После создания у пользователя должен быть id");
        assertEquals("test_user", createdUser.getUsername());
        assertEquals("testuser@gmail.com", createdUser.getEmail());
    }

    @Test
    void createUser_checkThatUserInDb() {
        User user = new User();
        user.setUsername("test_user");
        user.setEmail("testuser@gmail.com");
        User createdUser = userDao.createUser(user);
        User userFromDb = userDao.findById(createdUser.getId());
        assertNotNull(userFromDb, "Пользователь должен существовать в БД");
        assertEquals("test_user", userFromDb.getUsername());
        assertEquals("testuser@gmail.com", userFromDb.getEmail());
    }

    @Test
    void createUser_checkThatThrowIllegalArgumentExceptionWhenUserIdNotNull() {
        User newUser = new User(1L, "test_user", "testuser@gmail.com");
        assertThrows(IllegalArgumentException.class, () ->
                        userDao.createUser(newUser),
                "При создании пользователя с уже заданным id должен быть IllegalArgumentException"
        );
    }

    @Test
    void createUser_checkThatThrowDaoExceptionWhenResultSetIsEmpty() throws SQLException {
        User user = new User();
        user.setUsername("test_user");
        user.setEmail("testuser@gmail.com");

        try (MockedStatic<ConnectionManager> mockedStatic = Mockito.mockStatic(ConnectionManager.class)) {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            ResultSet mockRs = mock(ResultSet.class);
            mockedStatic.when(ConnectionManager::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPs);
            when(mockPs.executeQuery()).thenReturn(mockRs);
            when(mockRs.next()).thenReturn(false);
            assertThrows(DaoException.class,
                    () -> userDao.createUser(user),
                    "Если ResultSet пустой, должен быть DaoException"
            );

            verify(mockPs).executeQuery();
        }
    }

    @Test
    void createUser_checkThatWrapSqlExceptionIntoDaoException() {
        User user = new User();
        user.setUsername("test_user");
        user.setEmail("testuser@gmail.com");

        try (MockedStatic<ConnectionManager> mockedStatic = Mockito.mockStatic(ConnectionManager.class)) {
            mockedStatic.when(ConnectionManager::getConnection)
                    .thenThrow(new SQLException("Test SQL error"));

            assertThrows(DaoException.class,
                    () -> userDao.createUser(user),
                    "При SQLException должен быть брошен DaoException"
            );
        }
    }

    @Test
    void findAllUsers_checkThatReturnListUsers() {
        User user1 = new User();
        user1.setUsername("test_user1");
        user1.setEmail("testuser1@gmail.com");
        userDao.createUser(user1);

        User user2 = new User();
        user2.setUsername("test_user2");
        user2.setEmail("testuser2@gmail.com");
        userDao.createUser(user2);

        List<User> users = userDao.findAllUsers();

        assertEquals(2, users.size(), "Должно вернуться 2 пользователя");

        boolean testUser1 = users.stream()
                .anyMatch(u -> u.getUsername().equals("test_user1")
                        && u.getEmail().equals("testuser1@gmail.com")
                );

        boolean testUser2 = users.stream()
                .anyMatch(u -> u.getUsername().equals("test_user2")
                        && u.getEmail().equals("testuser2@gmail.com")
                );

        assertTrue(testUser1, "Среди пользователей должен быть test_user1");
        assertTrue(testUser2, "Среди пользователей должен быть test_user2");
    }

    @Test
    void findAllUsers_checkThatWrapSqlExceptionIntoDaoException() {
        User user1 = new User();
        user1.setUsername("test_user1");
        user1.setEmail("testuser1@gmail.com");
        userDao.createUser(user1);

        User user2 = new User();
        user2.setUsername("test_user2");
        user2.setEmail("testuser2@gmail.com");
        userDao.createUser(user2);

        try (MockedStatic<ConnectionManager> mockedStatic = Mockito.mockStatic(ConnectionManager.class)) {
            mockedStatic.when(ConnectionManager::getConnection)
                    .thenThrow(new SQLException("Test SQL error"));

            assertThrows(DaoException.class,
                    () -> userDao.findAllUsers(),
                    "При SQLException должен быть брошен DaoException"
            );
        }
    }

    @Test
    void findById_checkThatReturnNullWhenUserDoesNotExist() {
        Long notExistingId = Long.MAX_VALUE;
        User userById = userDao.findById(notExistingId);
        assertNull(userById, "Если пользователя в БД нет, findById должен вернуть null");
    }

    @Test
    void findById_checkThatWrapSqlExceptionIntoDaoException() {
        User user = new User();
        user.setUsername("test_user");
        user.setEmail("testuser@gmail.com");
        User createdUser = userDao.createUser(user);

        try (MockedStatic<ConnectionManager> mockedStatic = Mockito.mockStatic(ConnectionManager.class)) {
            mockedStatic.when(ConnectionManager::getConnection)
                    .thenThrow(new SQLException("Test SQL error"));

            assertThrows(DaoException.class,
                    () -> userDao.findById(createdUser.getId()),
                    "When SQLException is thrown, a DaoException must be thrown."
            );
        }
    }

    @Test
    void updateUser_checkThatThrowIllegalArgumentExceptionWhenUserIdIsNull() {
        User user = new User();
        user.setUsername("test_user");
        user.setEmail("testuser@gmail.com");

        assertThrows(IllegalArgumentException.class,
                () -> userDao.updateUser(user),
                "При обновлении пользователя без id должен быть IllegalArgumentException"
        );
    }

    @Test
    void updateUser_checkThatChangeUserData() {
        User user = new User();
        user.setUsername("test_user");
        user.setEmail("testuser@gmail.com");
        userDao.createUser(user);

        user.setUsername("updated_user");
        user.setEmail("updateduser@gmail.com");
        userDao.updateUser(user);

        User userFromDb = userDao.findById(user.getId());
        assertNotNull(userFromDb, "После обновления пользователь должен существовать");
        assertEquals("updated_user", userFromDb.getUsername());
        assertEquals("updateduser@gmail.com", userFromDb.getEmail());
    }

    @Test
    void updateUser_checkThatThrowDaoExceptionWhenUserNotFoundById() throws SQLException {
        User user = new User();
        user.setId(Long.MAX_VALUE);
        user.setUsername("test_user");
        user.setEmail("testuser@gmail.com");

        assertThrows(DaoException.class,
                () -> userDao.updateUser(user),
                "Если пользователь не найден по id, должен быть DaoException"
        );
    }

    @Test
    void updateUser_checkThatWrapSqlExceptionIntoDaoException() {
        User user = new User();
        user.setUsername("test_user");
        user.setEmail("testuser@gmail.com");
        userDao.createUser(user);

        try (MockedStatic<ConnectionManager> mockedStatic = Mockito.mockStatic(ConnectionManager.class)) {
            mockedStatic.when(ConnectionManager::getConnection)
                    .thenThrow(new SQLException("Test SQL error"));

            assertThrows(DaoException.class,
                    () -> userDao.updateUser(user),
                    "При SQLException должен быть брошен DaoException"
            );
        }
    }

    @Test
    void deleteById_checkThatUserRemoved() {
        User user = new User();
        user.setUsername("test_user");
        user.setEmail("testuser@gmail.com");
        userDao.createUser(user);

        Long id = user.getId();
        userDao.deleteById(id);
        User deletedUser = userDao.findById(id);
        assertNull(deletedUser, "После удаления пользователь не должен существовать в БД");
    }

    @Test
    void deleteById_checkThatThrowDaoExceptionWhenUserWasNotDeleteById() {
        Long notExistingId = Long.MAX_VALUE;

        assertThrows(DaoException.class,
                () -> userDao.deleteById(notExistingId),
                "Если пользователь не найден по id, должен быть DaoException"
        );
    }

    @Test
    void deleteById_checkThatWrapSqlExceptionIntoDaoException() {
        User user = new User();
        user.setUsername("test_user");
        user.setEmail("testuser@gmail.com");
        userDao.createUser(user);

        try (MockedStatic<ConnectionManager> mockedStatic = Mockito.mockStatic(ConnectionManager.class)) {
            mockedStatic.when(ConnectionManager::getConnection)
                    .thenThrow(new SQLException("Test SQL error"));

            assertThrows(DaoException.class,
                    () -> userDao.deleteById(user.getId()),
                    "При SQLException должен быть брошен DaoException"
            );
        }
    }
}
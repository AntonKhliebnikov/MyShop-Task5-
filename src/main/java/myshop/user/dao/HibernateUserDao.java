package myshop.user.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lombok.extern.log4j.Log4j2;
import myshop.common.exception.DaoException;
import myshop.common.jpa.JpaUtil;
import myshop.user.model.User;

import java.util.List;

@Log4j2
public class HibernateUserDao implements UserDao {
    @Override
    public User createUser(User user) {
        if (user.getId() != null) {
            throw new IllegalArgumentException("When creating a user, the id must be null");
        }

        log.debug("createUser() called with user = {}", user);

        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();
            em.persist(user);
            transaction.commit();
            log.info("User successfully created with id = {}", user.getId());
            return user;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            log.error("Error creating user: {}", user, e);
            throw new DaoException("Error creating user: " + user, e);
        } finally {
            em.close();
        }
    }

    @Override
    public List<User> findAllUsers() {
        log.debug("findAllUsers() called");
        EntityManager em = JpaUtil.getEntityManager();

        try {
            String jpql = "SELECT u FROM User u";
            List<User> userList = em.createQuery(jpql, User.class).getResultList();
            log.info("{} userList found", userList.size());
            return userList;
        } catch (Exception e) {
            log.error("Error getting all users", e);
            throw new DaoException("Error getting all users", e);
        } finally {
            em.close();
        }
    }


    @Override
    public User findById(Long id) {
        log.debug("findById() called with id = {}", id);
        EntityManager em = JpaUtil.getEntityManager();

        try {

            User foundUser = em.find(User.class, id);
            if (foundUser == null) {
                throw new DaoException("User with id = " + id + " not found");
            }

            log.info("User with id = {} found: {}", id, foundUser);
            return foundUser;
        } catch (Exception e) {
            log.error("Error finding user by id = {}", id, e);
            throw new DaoException("Error finding user by id = " + id, e);
        } finally {
            em.close();
        }
    }

    @Override
    public void updateUser(User user) {
        if (user.getId() == null) {
            throw new IllegalArgumentException("You can't update user without id");
        }

        log.debug("updateUser() called for user = {}", user);

        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();
            em.merge(user);
            transaction.commit();
            log.info("User with id = {} successfully updated", user.getId());
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            log.error("Error updating user: {}", user, e);
            throw new DaoException("Error updating user: " + user, e);
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteById(Long id) {
        log.debug("deleteById() called with id = {}", id);
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();

            User userToDelete = em.find(User.class, id);
            if (userToDelete == null) {
                throw new DaoException("User with id = " + id + " not found, delete failed");
            }

            em.remove(userToDelete);
            transaction.commit();
            log.info("User with id = {} successfully deleted", id);
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            log.error("Error deleting user by id = {}", id, e);
            throw new DaoException("Error deleting user by id = " + id, e);
        } finally {
            em.close();
        }
    }
}
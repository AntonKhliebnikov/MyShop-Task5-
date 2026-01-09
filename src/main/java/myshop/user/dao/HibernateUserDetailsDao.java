package myshop.user.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lombok.extern.log4j.Log4j2;
import myshop.common.exception.DaoException;
import myshop.common.jpa.JpaUtil;
import myshop.user.model.UserDetails;

import java.util.List;

@Log4j2
public class HibernateUserDetailsDao implements UserDetailsDao {
    @Override
    public void createUserDetails(UserDetails userDetails) {
        if (userDetails.getUserId() == null) {
            throw new IllegalArgumentException("When creating userDetails, userId must not be null.");
        }

        log.debug("createUserDetails() called with userDetails = {}", userDetails);

        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();
            em.persist(userDetails);
            transaction.commit();
            log.info("userDetails: {} created for userId = {}, ",
                    userDetails, userDetails.getUserId());
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            log.error("Error creating userDetails: {}", userDetails, e);
            throw new DaoException("Error creating userDetails: " + userDetails, e);
        } finally {
            em.close();
        }
    }

    @Override
    public List<UserDetails> findAllUserDetails() {
        log.debug("findAllUserDetails() called");

        EntityManager em = JpaUtil.getEntityManager();
        try {
            String jpql = "SELECT ud FROM UserDetails ud";
            List<UserDetails> userDetailsList = em.createQuery(jpql, UserDetails.class).getResultList();
            log.info("{} entries found in userDetails", userDetailsList.size());
            return userDetailsList;
        } catch (Exception e) {
            log.error("Error getting all userDetails", e);
            throw new DaoException("Error getting all userDetails", e);
        } finally {
            em.close();
        }
    }

    @Override
    public UserDetails findByUserId(Long userId) {
        log.debug("findByUserId() called with userId = {}", userId);

        EntityManager em = JpaUtil.getEntityManager();

        try {
            UserDetails foundUserDetails = em.find(UserDetails.class, userId);
            if (foundUserDetails == null) {
                throw new DaoException("UserDetails with userId = " + userId + " not found");
            }

            log.info("UserDetails for userId = {} found: {}", userId, foundUserDetails);
            return foundUserDetails;
        } catch (Exception e) {
            log.error("Error finding userDetails by userId = {}", userId, e);
            throw new DaoException("Error finding userDetails by userId = " + userId, e);
        } finally {
            em.close();
        }
    }

    @Override
    public void updateUserDetails(UserDetails userDetails) {
        if (userDetails.getUserId() == null) {
            throw new IllegalArgumentException("You can't update userDetails without userId");
        }

        log.debug("updateUserDetails() called with userDetails = {}", userDetails);

        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();
            em.merge(userDetails);
            transaction.commit();
            log.info("UserDetails with userId = {} has been updated successfully",
                    userDetails.getUserId());
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            log.error("Error updating userDetails: {}", userDetails, e);
            throw new DaoException("Error updating userDetails: " + userDetails, e);
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteByUserId(Long userId) {
        log.debug("deleteByUserId() called with userId = {}", userId);

        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();

            UserDetails userDetailsToDelete = em.find(UserDetails.class, userId);
            if (userDetailsToDelete == null) {
                throw new DaoException("UserDetails with userId = " + userId + " not found, delete failed");
            }

            em.remove(userDetailsToDelete);
            transaction.commit();
            log.info("UserDetails with userId = {} successfully deleted", userId);
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            log.error("Error deleting userDetails by userId = {}", userId, e);
            throw new DaoException("Error deleting userDetails by userId = " + userId, e);
        } finally {
            em.close();
        }
    }
}
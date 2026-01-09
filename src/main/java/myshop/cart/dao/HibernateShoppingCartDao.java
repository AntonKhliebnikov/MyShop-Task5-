package myshop.cart.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lombok.extern.log4j.Log4j2;
import myshop.cart.model.ShoppingCart;
import myshop.cart.model.ShoppingCartId;
import myshop.common.exception.DaoException;
import myshop.common.jpa.JpaUtil;

import java.util.List;

@Log4j2
public class HibernateShoppingCartDao implements ShoppingCartDao {
    @Override
    public void addProduct(Long userId, Long productId, Integer quantity) {
        if (userId == null || productId == null) {
            throw new IllegalArgumentException("userId and productId must not be null");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("quantity must be > 0");
        }

        log.debug("addProduct() called with userId = {}, productId = {}, quantity = {}.",
                userId, productId, quantity);
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();

            ShoppingCartId id = new ShoppingCartId(userId, productId);
            ShoppingCart shoppingCart = em.find(ShoppingCart.class, id);

            if (shoppingCart == null) {
                shoppingCart = new ShoppingCart(userId, productId, quantity);
                em.persist(shoppingCart);
                log.info("ShoppingCart created: {}", shoppingCart);
            } else {
                shoppingCart.setQuantity(shoppingCart.getQuantity() + quantity);
                em.merge(shoppingCart);
                log.info("ShoppingCart updated (quantity increased): {}", shoppingCart);
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            log.error("Error adding product to cart (userId = {}, productId = {})",
                    userId, productId, e);
            throw new DaoException("Error adding product to cart", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void removeProduct(Long userId, Long productId) {
        if (userId == null || productId == null) {
            throw new IllegalArgumentException("userId and productId must not be null");
        }

        log.debug("removeProduct() called with userId = {}, productId = {}", userId, productId);

        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();

            ShoppingCartId id = new ShoppingCartId(userId, productId);
            ShoppingCart shoppingCart = em.find(ShoppingCart.class, id);

            if (shoppingCart == null) {
                throw new DaoException("ShoppingCart not found for userId = " +
                        userId + ", productId = " + productId);
            }

            em.remove(shoppingCart);
            transaction.commit();
            log.info("ShoppingCart removed for userId = {}, productId = {}", userId, productId);
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            log.error("Error removing product from cart (userId = {}, productId = {})",
                    userId, productId, e);
            throw new DaoException("Error removing product from cart", e);
        } finally {
            em.close();
        }
    }

    @Override
    public List<ShoppingCart> findByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }

        log.debug("findByUserId() called with userId = {}", userId);

        EntityManager em = JpaUtil.getEntityManager();

        try {
            String jpql = "SELECT sc FROM ShoppingCart sc WHERE sc.userId = :userId";
            List<ShoppingCart> items = em.createQuery(jpql, ShoppingCart.class)
                    .setParameter("userId", userId)
                    .getResultList();

            log.info("{} cart items found for userId = {}", items.size(), userId);
            return items;
        } catch (Exception e) {
            log.error("Error getting cart items for userId = {}", userId, e);
            throw new DaoException("Error getting cart items for userId = " + userId, e);
        } finally {
            em.close();
        }
    }

    @Override
    public void clearCart(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }

        log.debug("clearCart() called with userId = {}", userId);

        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            String jpql = "DELETE FROM ShoppingCart sc WHERE sc.userId = :userId";
            int deleted = em.createQuery(jpql)
                    .setParameter("userId", userId)
                    .executeUpdate();

            tx.commit();
            log.info("Cart cleared for userId = {} ({} items deleted)", userId, deleted);
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            log.error("Error clearing cart for userId = {}", userId, e);
            throw new DaoException("Error clearing cart for userId = " + userId, e);
        } finally {
            em.close();
        }
    }
}
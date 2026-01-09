package myshop.order.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lombok.extern.log4j.Log4j2;
import myshop.common.exception.DaoException;
import myshop.common.jpa.JpaUtil;
import myshop.order.model.Order;

import java.util.List;

@Log4j2
public class HibernateOrderDao implements OrderDao {
    @Override
    public Order saveOrder(Order order) {
        if (order.getId() != null) {
            throw new IllegalArgumentException("When creating an order, the id must be null");
        }

        log.debug("saveOrder() called with order = {}", order);
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();
            em.persist(order);
            transaction.commit();
            log.info("Order successfully created, id = {}", order.getId());
            return order;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            log.error("Error saving order: {}", order, e);
            throw new DaoException("Error saving order: " + order, e);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Order> findAllOrdersByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }

        log.debug("findAllOrdersByUserId() called with userId = {}", userId);
        EntityManager em = JpaUtil.getEntityManager();

        try {
            String jpql = "SELECT o FROM Order o WHERE o.userId = :userId";
            List<Order> userOrders = em.createQuery(jpql, Order.class)
                    .setParameter("userId", userId)
                    .getResultList();
            log.info("{} userOrders found", userOrders.size());
            return userOrders;
        } catch (Exception e) {
            log.error("Error getting all userOrders by userId = {}", userId, e);
            throw new DaoException("Error getting all userOrders by userId = " + userId, e);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Order> findAllOrders() {
        log.debug("findAllOrders() called");
        EntityManager em = JpaUtil.getEntityManager();

        try {
            String jpql = "SELECT o FROM Order o";
            List<Order> orders = em.createQuery(jpql, Order.class).getResultList();
            log.info("{} orders found", orders.size());
            return orders;
        } catch (Exception e) {
            log.error("Error receiving all orders", e);
            throw new DaoException("Error receiving all orders", e);
        } finally {
            em.close();
        }
    }
}
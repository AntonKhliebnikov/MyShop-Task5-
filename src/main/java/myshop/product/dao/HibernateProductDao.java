package myshop.product.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lombok.extern.log4j.Log4j2;
import myshop.common.exception.DaoException;
import myshop.common.jpa.JpaUtil;
import myshop.product.model.Product;

import java.util.List;

@Log4j2
public class HibernateProductDao implements ProductDao {
    @Override
    public Product createProduct(Product product) {
        if (product.getId() != null) {
            throw new IllegalArgumentException("When creating a product, the id must be null");
        }

        log.debug("createProduct() called with product = {}", product);

        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();
            em.persist(product);
            transaction.commit();
            log.info("Product successfully created with id = {}", product.getId());
            return product;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            log.error("Error creating product: {}", product, e);
            throw new DaoException("Error creating product: " + product, e);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Product> findAllProducts() {
        log.debug("findAllProducts() called");
        EntityManager em = JpaUtil.getEntityManager();

        try {
            String jpql = "SELECT p FROM Product p";
            List<Product> productList = em.createQuery(jpql, Product.class).getResultList();
            log.info("{} products found", productList.size());
            return productList;
        } catch (Exception e) {
            log.error("Error getting all products", e);
            throw new DaoException("Error getting all products", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Product findById(Long id) {
        log.debug("findById() called with id = {}", id);
        EntityManager em = JpaUtil.getEntityManager();

        try {
            Product foundProduct = em.find(Product.class, id);
            if (foundProduct == null) {
                throw new DaoException("Product with id = " + id + " not found");
            }

            log.info("Product with id = {} found: {}", id, foundProduct);
            return foundProduct;
        } catch (Exception e) {
            log.error("Error finding product by id = {}", id, e);
            throw new DaoException("Error finding product by id = " + id, e);
        } finally {
            em.close();
        }
    }

    @Override
    public void updateProduct(Product product) {
        if (product.getId() == null) {
            throw new IllegalArgumentException("You can't update product without id");
        }

        log.debug("updateProduct() called for product = {}", product);
        EntityManager em = JpaUtil.getEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();
            em.merge(product);
            transaction.commit();
            log.info("Product with id = {} successfully updated", product.getId());
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
                log.error("Error updating product: {}", product, e);
                throw new DaoException("Error updating product: " + product, e);
            }
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

            Product productToDelete = em.find(Product.class, id);
            if (productToDelete == null) {
                throw new DaoException("Product with id = " + id + " not found, delete failed");
            }

            em.remove(productToDelete);
            transaction.commit();
            log.info("Product with id = {} successfully deleted", id);
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            log.error("Error deleting product by id = {}", id, e);
            throw new DaoException("Error deleting product by id = " + id, e);
        } finally {
            em.close();
        }
    }
}
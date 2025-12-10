package myshop.product.dao;

import myshop.product.model.Product;

import java.util.List;

public interface ProductDao {
    Product createProduct(Product product);

    List<Product> findAllProducts();

    Product findById(Long id);

    void updateProduct(Product product);

    void deleteById(Long id);
}

package myshop.cart.dao;

import myshop.cart.model.ShoppingCart;

import java.util.List;

public interface ShoppingCartDao {
    void addProduct(Long userId, Long productId, Integer quantity);

    void removeProduct(Long userId, Long productId);

    List<ShoppingCart> findByUserId(Long userId);

    void clearCart(Long userId);
}

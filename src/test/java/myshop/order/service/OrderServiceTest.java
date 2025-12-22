package myshop.order.service;

import myshop.cart.dao.ShoppingCartDao;
import myshop.cart.model.ShoppingCart;
import myshop.order.dao.OrderDao;
import myshop.order.model.Order;
import myshop.product.dao.ProductDao;
import myshop.product.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Test
    void placeOrder_chackThatOrderHasBeenCreatedSavedInTheDbAndTheCartHasBeenCleared() {
        ShoppingCartDao shoppingCartDao = mock(ShoppingCartDao.class);
        ProductDao productDao = mock(ProductDao.class);
        OrderDao orderDao = mock(OrderDao.class);
        OrderService orderService = new OrderService(shoppingCartDao, productDao, orderDao);
        Long userId = 1L;
        ShoppingCart userShoppingCart1 = new ShoppingCart(userId, 1L, 1);
        ShoppingCart userShoppingCart2 = new ShoppingCart(userId, 2L, 2);
        List<ShoppingCart> userProducts = List.of(userShoppingCart1, userShoppingCart2);
        when(shoppingCartDao.findByUserId(userId)).thenReturn(userProducts);
        when(productDao.findById(1L))
                .thenReturn(new Product(1L, "product1", new BigDecimal("100.00")));
        when(productDao.findById(2L))
                .thenReturn(new Product(2L, "product2", new BigDecimal("50.00")));

        Order order = orderService.placeOrder(userId);
        assertEquals(new BigDecimal("200.00"), order.getTotalAmount(),
                "The total order amount must be 200.00"
        );

        String orderedProducts = order.getOrderedProducts();
        assertTrue(orderedProducts.contains("product1"));
        assertTrue(orderedProducts.contains("product2"));

        verify(shoppingCartDao, times(1)).findByUserId(userId);
        verify(orderDao, times(1)).saveOrder(any(Order.class));
        verify(shoppingCartDao, times(1)).clearCart(userId);
    }
}
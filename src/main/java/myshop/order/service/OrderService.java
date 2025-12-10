package myshop.order.service;

import myshop.cart.dao.ShoppingCartDao;
import myshop.cart.model.ShoppingCart;
import myshop.order.dao.OrderDao;
import myshop.order.model.Order;
import myshop.product.dao.ProductDao;
import myshop.product.model.Product;

import java.math.BigDecimal;
import java.util.List;

public class OrderService {
    private final ShoppingCartDao cartDao;
    private final ProductDao productDao;
    private final OrderDao orderDao;

    public OrderService(ShoppingCartDao cartDao, ProductDao productDao, OrderDao orderDao) {
        this.cartDao = cartDao;
        this.productDao = productDao;
        this.orderDao = orderDao;
    }

    public Order placeOrder(Long userId) {
        List<ShoppingCart> userCarts = cartDao.findByUserId(userId);
        StringBuilder builder = new StringBuilder();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (ShoppingCart cart : userCarts) {
            Long productId = cart.getProductId();
            Product product = productDao.findById(productId);
            String productName = product.getProductName();
            builder.append(productName).append(", ");
            Integer itemQuantity = cart.getQuantity();
            BigDecimal itemPrice = product.getPrice();
            BigDecimal itemTotalPrice = itemPrice.multiply(BigDecimal.valueOf(itemQuantity));
            totalAmount = totalAmount.add(itemTotalPrice);
        }

        if (!userCarts.isEmpty()) {
            builder.setLength(builder.length() - 2);
        }

        Order order = new Order(userId, builder.toString(), totalAmount);
        orderDao.saveOrder(order);
        cartDao.clearCart(userId);
        return order;
    }
}
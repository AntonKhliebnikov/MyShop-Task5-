package myshop.order.service;

import lombok.extern.log4j.Log4j2;
import myshop.cart.dao.ShoppingCartDao;
import myshop.cart.model.ShoppingCart;
import myshop.order.dao.OrderDao;
import myshop.order.model.Order;
import myshop.product.dao.ProductDao;
import myshop.product.model.Product;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.List;

@Log4j2
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
        log.debug("placeOrder() called with userId = {}", userId);

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
            log.debug("Added product '{}' (id = {}) x{} to order for user with userId = {}. Item totalAmount = {}.",
                    productName, productId, itemQuantity, userId, itemTotalPrice);
        }

        if (!userCarts.isEmpty()) {
            builder.setLength(builder.length() - 2);
        }

        Order order = new Order(userId, builder.toString(), totalAmount);
        log.debug("Order object before saving: {}", order);
        Order savedOrder = orderDao.saveOrder(order);
        cartDao.clearCart(userId);
        log.info("Order {} successfully placed for user {}. Total amount = {}.",
                savedOrder.getId(), userId, totalAmount);

        return savedOrder;
    }
}
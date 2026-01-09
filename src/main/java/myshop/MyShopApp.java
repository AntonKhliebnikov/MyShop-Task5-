package myshop;

import myshop.cart.dao.HibernateShoppingCartDao;
import myshop.cart.dao.ShoppingCartDao;
import myshop.order.dao.HibernateOrderDao;
import myshop.order.dao.OrderDao;
import myshop.order.service.OrderService;
import myshop.product.dao.HibernateProductDao;
import myshop.product.dao.ProductDao;
import myshop.product.model.Product;

import java.math.BigDecimal;

public class MyShopApp {
    public static void main(String[] args) {
//        ShoppingCartDao cartDao = new HibernateShoppingCartDao();
//        ProductDao productDao = new HibernateProductDao();
//        OrderDao orderDao = new HibernateOrderDao();
//        OrderService orderService = new OrderService(cartDao, productDao, orderDao);
//        orderService.placeOrder(10L);
        Product product = new Product();
        product.setProductName("product1");
        product.setPrice(new BigDecimal("100.00"));
        ProductDao productDao = new HibernateProductDao();
        productDao.createProduct(product);
    }
}

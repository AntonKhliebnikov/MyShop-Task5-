package myshop;

import myshop.cart.dao.JdbcShoppingCartDao;
import myshop.cart.dao.ShoppingCartDao;
import myshop.order.dao.JdbcOrderDao;
import myshop.order.dao.OrderDao;
import myshop.order.service.OrderService;
import myshop.product.dao.JdbcProductDao;
import myshop.product.dao.ProductDao;
import myshop.user.dao.JdbcUserDao;
import myshop.user.dao.UserDao;
import myshop.user.model.User;

public class MyShopApp {
    public static void main(String[] args) {
        ShoppingCartDao cartDao = new JdbcShoppingCartDao();
        ProductDao productDao = new JdbcProductDao();
        OrderDao orderDao = new JdbcOrderDao();
//        OrderService orderService = new OrderService(cartDao, productDao, orderDao);
//        System.out.println(orderService.placeOrder(9L));
        User user = new User(null, "Anton", "toha@gmail.com");
        UserDao userDao = new JdbcUserDao();
        System.out.println(userDao.createUser(user));
    }
}

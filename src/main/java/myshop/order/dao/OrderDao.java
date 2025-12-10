package myshop.order.dao;

import myshop.order.model.Order;

import java.util.List;

public interface OrderDao {
    Order saveOrder(Order order);

    List<Order> findAllOrdersByUserId(Long userId);

    List<Order> findAllOrders();
}

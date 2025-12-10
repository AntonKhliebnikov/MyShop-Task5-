package myshop.order.model;

import java.math.BigDecimal;

public class Order {
    private Long id;
    private Long userId;
    private String orderedProducts;
    private BigDecimal totalAmount;

    public Order(Long userId, String orderedProducts, BigDecimal totalAmount) {
        this.userId = userId;
        this.orderedProducts = orderedProducts;
        this.totalAmount = totalAmount;
    }

    public Order(Long id, Long userId, String orderedProducts, BigDecimal totalAmount) {
        this.id = id;
        this.userId = userId;
        this.orderedProducts = orderedProducts;
        this.totalAmount = totalAmount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getOrderedProducts() {
        return orderedProducts;
    }

    public void setOrderedProducts(String orderedProducts) {
        this.orderedProducts = orderedProducts;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", userId=" + userId +
                ", orderedProducts='" + orderedProducts + '\'' +
                ", totalAmount=" + totalAmount +
                '}';
    }
}

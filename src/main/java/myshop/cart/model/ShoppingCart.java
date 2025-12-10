package myshop.cart.model;

public class ShoppingCart {
    private Long userId;
    private Long productId;
    private Integer quantity;

    public ShoppingCart(Long userId, Long productId, Integer quantity) {
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "ShoppingCart{" +
                "userId=" + userId +
                ", productId=" + productId +
                ", quantity=" + quantity +
                '}';
    }
}
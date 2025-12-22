package myshop.order.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
@NoArgsConstructor
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
}

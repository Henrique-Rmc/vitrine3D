package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.OrderItem;

import java.math.BigDecimal;

// DORMANT — not wired into the system yet.
public class OrderItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal subtotal;

    public static OrderItemResponse from(OrderItem item) {
        OrderItemResponse dto = new OrderItemResponse();
        dto.id = item.getId();
        dto.productId = item.getProductId();
        dto.productName = item.getProductName();
        dto.unitPrice = item.getUnitPrice();
        dto.quantity = item.getQuantity();
        dto.subtotal = item.getSubtotal();
        return dto;
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
    public BigDecimal getSubtotal() { return subtotal; }
}

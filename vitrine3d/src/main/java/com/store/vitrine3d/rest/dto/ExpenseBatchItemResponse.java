package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.ExpenseBatchItem;

import java.math.BigDecimal;

public class ExpenseBatchItemResponse {

    private Long id;
    private Long expenseProductId;
    private String productName;
    private String unit;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal subtotal;

    public static ExpenseBatchItemResponse from(ExpenseBatchItem item) {
        ExpenseBatchItemResponse dto = new ExpenseBatchItemResponse();
        dto.id = item.getId();
        dto.expenseProductId = item.getExpenseProductId();
        dto.productName = item.getProductName();
        dto.unit = item.getUnit();
        dto.unitPrice = item.getUnitPrice();
        dto.quantity = item.getQuantity();
        dto.subtotal = item.getSubtotal();
        return dto;
    }

    public Long getId() { return id; }
    public Long getExpenseProductId() { return expenseProductId; }
    public String getProductName() { return productName; }
    public String getUnit() { return unit; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
    public BigDecimal getSubtotal() { return subtotal; }
}

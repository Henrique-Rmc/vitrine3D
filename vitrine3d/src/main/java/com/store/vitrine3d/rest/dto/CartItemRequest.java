package com.store.vitrine3d.rest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// DORMANT — not wired into the system yet.
public class CartItemRequest {

    @NotNull
    private Long productId;

    @Min(1)
    private int quantity = 1;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}

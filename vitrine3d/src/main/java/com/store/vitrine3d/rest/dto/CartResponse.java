package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.Cart;
import com.store.vitrine3d.domain.model.CartStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

// DORMANT — not wired into the system yet.
public class CartResponse {

    private UUID id;
    private UUID storeId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private CartStatus status;
    private List<CartItemResponse> items;
    private BigDecimal total;
    private Instant createdAt;
    private Instant updatedAt;

    public static CartResponse from(Cart cart) {
        CartResponse dto = new CartResponse();
        dto.id = cart.getId();
        dto.storeId = cart.getStore().getId();
        dto.customerName = cart.getCustomerName();
        dto.customerEmail = cart.getCustomerEmail();
        dto.customerPhone = cart.getCustomerPhone();
        dto.status = cart.getStatus();
        dto.items = cart.getItems().stream().map(CartItemResponse::from).toList();
        dto.total = cart.getItems().stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.createdAt = cart.getCreatedAt();
        dto.updatedAt = cart.getUpdatedAt();
        return dto;
    }

    public UUID getId() { return id; }
    public UUID getStoreId() { return storeId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public String getCustomerPhone() { return customerPhone; }
    public CartStatus getStatus() { return status; }
    public List<CartItemResponse> getItems() { return items; }
    public BigDecimal getTotal() { return total; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}

package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.Order;
import com.store.vitrine3d.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

// DORMANT — not wired into the system yet.
public class OrderResponse {

    private UUID id;
    private UUID storeId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String customerAddress;
    private String notes;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private List<OrderItemResponse> items;
    private Instant createdAt;
    private Instant updatedAt;

    public static OrderResponse from(Order order) {
        OrderResponse dto = new OrderResponse();
        dto.id = order.getId();
        dto.storeId = order.getStore().getId();
        dto.customerName = order.getCustomerName();
        dto.customerEmail = order.getCustomerEmail();
        dto.customerPhone = order.getCustomerPhone();
        dto.customerAddress = order.getCustomerAddress();
        dto.notes = order.getNotes();
        dto.status = order.getStatus();
        dto.totalAmount = order.getTotalAmount();
        dto.items = order.getItems().stream().map(OrderItemResponse::from).toList();
        dto.createdAt = order.getCreatedAt();
        dto.updatedAt = order.getUpdatedAt();
        return dto;
    }

    public UUID getId() { return id; }
    public UUID getStoreId() { return storeId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public String getCustomerPhone() { return customerPhone; }
    public String getCustomerAddress() { return customerAddress; }
    public String getNotes() { return notes; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public List<OrderItemResponse> getItems() { return items; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}

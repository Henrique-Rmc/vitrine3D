package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

// DORMANT — not wired into the system yet.
public class OrderStatusUpdateRequest {

    @NotNull
    private OrderStatus status;

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
}

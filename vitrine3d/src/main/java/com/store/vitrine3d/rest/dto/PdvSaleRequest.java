package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class PdvSaleRequest {
    @NotBlank
    private String offlineId;

    private UUID customerId;

    @NotNull
    private PaymentMethod paymentMethod;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal amountPaid;

    @NotNull
    private Instant saleDate;

    @NotEmpty
    private List<@Valid PdvSaleItemRequest> items;

    private String note;

    private UUID operatorId;
}

package com.store.vitrine3d.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class PdvCustomerCreditRequest {
    private UUID originSaleId;

    @NotBlank
    @Size(max = 255)
    private String productName;

    private Long productId;

    @DecimalMin("0.01")
    private BigDecimal originalAmount;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal totalDue;

    private Instant dueDate;

    @Size(max = 500)
    private String note;
}

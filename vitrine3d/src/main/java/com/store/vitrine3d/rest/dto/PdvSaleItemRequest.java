package com.store.vitrine3d.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PdvSaleItemRequest {
    private Long productId;

    @NotBlank
    private String productName;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal unitPrice;

    @Min(1)
    private int quantity;
}

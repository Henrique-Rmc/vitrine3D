package com.store.vitrine3d.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExpenseBatchItemRequest {

    @NotBlank
    private String productName;

    private String unit;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal unitPrice;

    @Min(1)
    private int quantity = 1;
}

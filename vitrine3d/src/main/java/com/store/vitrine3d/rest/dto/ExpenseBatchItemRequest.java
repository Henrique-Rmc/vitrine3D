package com.store.vitrine3d.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.store.vitrine3d.domain.model.MeasurementUnit;
import jakarta.validation.constraints.Digits;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExpenseBatchItemRequest {

    @NotBlank
    private String productName;

    private String unit;

    /** Unidade da quantidade comprada. Se omitida, assume a unidade do material. */
    private MeasurementUnit unitCode;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal unitPrice;

    @NotNull
    @DecimalMin("0.001")
    @Digits(integer = 11, fraction = 3)
    private BigDecimal quantity;
}

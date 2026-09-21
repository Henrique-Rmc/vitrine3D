package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.MeasurementUnit;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExpenseStockPatchRequest {

    /** Positivo = entrada, negativo = consumo. Expresso na unidade abaixo. */
    @NotNull
    @Digits(integer = 11, fraction = 3)
    private BigDecimal quantityDelta;

    /** Unidade do ajuste. Se omitida, assume a unidade do próprio material. */
    private MeasurementUnit unit;
}

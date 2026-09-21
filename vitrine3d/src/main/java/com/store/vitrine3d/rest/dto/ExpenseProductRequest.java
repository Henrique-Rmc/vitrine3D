package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.MeasurementUnit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExpenseProductRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    /** Rótulo livre de embalagem (ex.: "caixa"). Opcional, não participa de cálculo. */
    @Size(max = 50)
    private String unit;

    /** Unidade de medida do estoque. Se omitida, assume UN. */
    private MeasurementUnit stockUnit;

    /** Limite de alerta, na mesma unidade do estoque. Se omitido, mantém o atual. */
    @DecimalMin("0.000")
    @Digits(integer = 11, fraction = 3)
    private BigDecimal lowStockAlert;
}

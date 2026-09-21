package com.store.vitrine3d.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PdvProductCostRequest {

    @NotNull
    @DecimalMin(value = "0.00", message = "Custo nao pode ser negativo")
    @Digits(integer = 8, fraction = 2, message = "Formato de custo invalido")
    private BigDecimal costPrice;
}

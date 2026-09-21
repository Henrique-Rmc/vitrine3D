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

    // Preço de tabela (antes do desconto). Se omitido, assume igual a unitPrice.
    @DecimalMin("0.00")
    private BigDecimal originalUnitPrice;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal unitPrice;

    // Custo unitario. Se omitido, o backend usa o costPrice do produto no catalogo.
    // Permite ao cliente offline enviar o custo que tinha em cache.
    @DecimalMin("0.00")
    private BigDecimal unitCost;

    @Min(1)
    private int quantity;
}

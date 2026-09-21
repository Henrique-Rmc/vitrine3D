package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.Product;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Custo do produto — exposto apenas em rotas do PDV (plano Pro, autenticadas).
 * Nunca incluir esses campos em ProductResponse: aquela DTO serve rotas publicas
 * da vitrine e vazaria a margem da loja para qualquer visitante.
 */
@Data
public class PdvProductCostResponse {
    private Long productId;
    private String productName;
    private BigDecimal costPrice;
    private BigDecimal price;
    private BigDecimal marginAmount;
    private BigDecimal marginPercent;

    public static PdvProductCostResponse from(Product product) {
        PdvProductCostResponse dto = new PdvProductCostResponse();
        dto.setProductId(product.getId());
        dto.setProductName(product.getName());
        dto.setCostPrice(product.getCostPrice());
        dto.setPrice(product.getPrice());

        BigDecimal cost = product.getCostPrice();
        BigDecimal price = product.getPrice();
        if (cost != null && price != null && price.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal margin = price.subtract(cost);
            dto.setMarginAmount(margin);
            dto.setMarginPercent(margin
                    .multiply(BigDecimal.valueOf(100))
                    .divide(price, 2, RoundingMode.HALF_UP));
        }
        return dto;
    }
}

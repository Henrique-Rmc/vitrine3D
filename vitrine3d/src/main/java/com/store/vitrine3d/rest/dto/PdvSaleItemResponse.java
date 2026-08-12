package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.PdvSaleItem;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PdvSaleItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal subtotal;

    public static PdvSaleItemResponse from(PdvSaleItem item) {
        PdvSaleItemResponse dto = new PdvSaleItemResponse();
        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setProductName(item.getProductName());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setQuantity(item.getQuantity());
        dto.setSubtotal(item.getSubtotal());
        return dto;
    }
}

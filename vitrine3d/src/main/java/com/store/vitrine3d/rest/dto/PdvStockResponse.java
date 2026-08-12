package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.Product;
import lombok.Data;

@Data
public class PdvStockResponse {
    private Long productId;
    private String productName;
    private Boolean trackStock;
    private Integer stockQuantity;

    public static PdvStockResponse from(Product product) {
        PdvStockResponse dto = new PdvStockResponse();
        dto.setProductId(product.getId());
        dto.setProductName(product.getName());
        dto.setTrackStock(product.getTrackStock());
        dto.setStockQuantity(product.getStockQuantity());
        return dto;
    }
}

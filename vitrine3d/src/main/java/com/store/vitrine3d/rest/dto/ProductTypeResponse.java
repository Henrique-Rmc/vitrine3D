package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.ProductType;
import lombok.Data;

@Data
public class ProductTypeResponse {
    private Long id;
    private String key;
    private String label;
    private Integer sortOrder;

    public static ProductTypeResponse from(ProductType productType) {
        ProductTypeResponse dto = new ProductTypeResponse();
        dto.setId(productType.getId());
        dto.setKey(productType.getKey());
        dto.setLabel(productType.getLabel());
        dto.setSortOrder(productType.getSortOrder());
        return dto;
    }
}

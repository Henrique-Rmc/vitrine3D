package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.Category;
import lombok.Data;

@Data
public class CategoryResponse {

    private Long id;
    private String name;
    private Boolean isGlobal;
    private Long storeId;

    public static CategoryResponse from(Category category) {
        CategoryResponse dto = new CategoryResponse();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setIsGlobal(category.getIsGlobal());
        dto.setStoreId(category.getStore() != null ? category.getStore().getId() : null);
        return dto;
    }
}

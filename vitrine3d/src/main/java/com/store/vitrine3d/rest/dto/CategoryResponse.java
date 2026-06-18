package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.Category;
import lombok.Data;

import java.util.UUID;

@Data
public class CategoryResponse {

    private Long id;
    private String name;
    private Boolean isGlobal;
    private UUID storeId;

    public static CategoryResponse from(Category category) {
        CategoryResponse dto = new CategoryResponse();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setIsGlobal(category.getIsGlobal());
        dto.setStoreId(category.getStore() != null ? category.getStore().getId() : null);
        return dto;
    }
}

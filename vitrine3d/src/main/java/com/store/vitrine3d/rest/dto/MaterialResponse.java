package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.Material;
import lombok.Data;

import java.util.UUID;

@Data
public class MaterialResponse {

    private Long id;
    private String name;
    private Boolean isGlobal;
    private UUID storeId;

    public static MaterialResponse from(Material material) {
        MaterialResponse dto = new MaterialResponse();
        dto.setId(material.getId());
        dto.setName(material.getName());
        dto.setIsGlobal(material.getIsGlobal());
        dto.setStoreId(material.getStore() != null ? material.getStore().getId() : null);
        return dto;
    }
}

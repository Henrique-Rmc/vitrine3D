package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.LayoutMode;
import com.store.vitrine3d.domain.model.StoreTemplate;
import lombok.Data;

@Data
public class StoreTemplateResponse {
    private Long id;
    private String name;
    private String slug;
    private LayoutMode layoutMode;
    private String description;

    public static StoreTemplateResponse from(StoreTemplate t) {
        StoreTemplateResponse dto = new StoreTemplateResponse();
        dto.setId(t.getId());
        dto.setName(t.getName());
        dto.setSlug(t.getSlug());
        dto.setLayoutMode(t.getLayoutMode());
        dto.setDescription(t.getDescription());
        return dto;
    }
}

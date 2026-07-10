package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.BusinessType;
import lombok.Data;

@Data
public class BusinessTypeResponse {
    private Long id;
    private String name;
    private String slug;

    public static BusinessTypeResponse from(BusinessType businessType) {
        BusinessTypeResponse dto = new BusinessTypeResponse();
        dto.setId(businessType.getId());
        dto.setName(businessType.getName());
        dto.setSlug(businessType.getSlug());
        return dto;
    }
}

package com.store.vitrine3d.rest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryCreateRequest {

    @NotBlank
    private String name;

    private Boolean isGlobal;
    private Long storeId;
}

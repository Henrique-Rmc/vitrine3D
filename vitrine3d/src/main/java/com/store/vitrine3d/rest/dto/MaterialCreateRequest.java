package com.store.vitrine3d.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MaterialCreateRequest {

    @NotBlank(message = "Material name is required")
    @Size(min = 2, max = 100, message = "Material name must be between 2 and 100 characters")
    private String name;
}

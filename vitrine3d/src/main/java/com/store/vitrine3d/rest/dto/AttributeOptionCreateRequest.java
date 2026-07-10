package com.store.vitrine3d.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AttributeOptionCreateRequest {

    @NotBlank(message = "Value is required")
    @Size(max = 255, message = "Value must not exceed 255 characters")
    private String value;
}

package com.store.vitrine3d.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AttributeOptionRenameRequest {

    @NotBlank(message = "Old value is required")
    @Size(max = 255, message = "Old value must not exceed 255 characters")
    private String oldValue;

    @NotBlank(message = "New value is required")
    @Size(max = 255, message = "New value must not exceed 255 characters")
    private String newValue;
}

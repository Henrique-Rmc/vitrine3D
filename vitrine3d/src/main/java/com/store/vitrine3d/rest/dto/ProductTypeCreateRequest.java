package com.store.vitrine3d.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductTypeCreateRequest {

    @NotBlank(message = "Key is required")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9]*$", message = "Key must start with a letter and contain only letters/numbers")
    @Size(max = 100, message = "Key must not exceed 100 characters")
    private String key;

    @NotBlank(message = "Label is required")
    @Size(max = 100, message = "Label must not exceed 100 characters")
    private String label;

    private Integer sortOrder;
}

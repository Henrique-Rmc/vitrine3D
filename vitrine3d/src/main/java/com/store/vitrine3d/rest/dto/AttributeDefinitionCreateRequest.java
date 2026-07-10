package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.AttributeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class AttributeDefinitionCreateRequest {

    @NotBlank(message = "Key is required")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9]*$", message = "Key must start with a letter and contain only letters/numbers")
    @Size(max = 100, message = "Key must not exceed 100 characters")
    private String key;

    @NotBlank(message = "Label is required")
    @Size(max = 100, message = "Label must not exceed 100 characters")
    private String label;

    @NotNull(message = "Type is required")
    private AttributeType type;

    @Size(max = 50, message = "Unit must not exceed 50 characters")
    private String unit;

    private Boolean required;
    private Boolean filterable;
    private Integer sortOrder;
    private List<String> enumOptions;
}

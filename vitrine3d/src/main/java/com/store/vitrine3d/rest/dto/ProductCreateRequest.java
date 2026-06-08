package com.store.vitrine3d.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductCreateRequest {

    @NotBlank
    private String name;

    private String description;
    private String material;

    @NotNull
    private Boolean multicolor;

    private String dimensions;

    @NotNull
    private Long categoryId;

    @NotNull
    private Long storeId;
}

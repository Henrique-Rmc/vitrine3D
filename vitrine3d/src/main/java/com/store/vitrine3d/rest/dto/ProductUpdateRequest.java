package com.store.vitrine3d.rest.dto;

import lombok.Data;

@Data
public class ProductUpdateRequest {
    private String name;
    private String description;
    private String material;
    private Boolean multicolor;
    private String dimensions;
    private Long categoryId;
    private Boolean isVisible;
    private Boolean featured;
}

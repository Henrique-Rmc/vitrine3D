package com.store.vitrine3d.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductUpdateRequest {

    @Size(min = 2, max = 150, message = "Product name must be between 2 and 150 characters")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @Size(max = 100, message = "Material must not exceed 100 characters")
    private String material;

    private Boolean multicolor;

    @Size(max = 100, message = "Dimensions must not exceed 100 characters")
    private String dimensions;

    private Long categoryId;
    private Boolean isVisible;
    private Boolean featured;

    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Invalid price format (max 8 integer digits, 2 decimal)")
    private BigDecimal price;
}

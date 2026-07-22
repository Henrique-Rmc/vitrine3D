package com.store.vitrine3d.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class ProductCreateRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 150, message = "Product name must be between 2 and 150 characters")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Store is required")
    private UUID storeId;

    private Boolean isVisible;

    /** Fallback pra passar URLs ja hospedadas diretamente (sem upload de arquivo). */
    private List<@Size(max = 2048, message = "Image URL must not exceed 2048 characters") String> imageUrls;

    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Invalid price format (max 8 integer digits, 2 decimal)")
    private BigDecimal price;

    /** Atributos dinamicos do tipo de negocio da loja (ex.: marca, ano, kilometragem). */
    private Map<String, Object> attributes;

    @NotNull(message = "Product type is required")
    private Long productTypeId;
}

package com.store.vitrine3d.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFilter {
    private String keyword;
    private Long categoryId;
    private Long materialId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}

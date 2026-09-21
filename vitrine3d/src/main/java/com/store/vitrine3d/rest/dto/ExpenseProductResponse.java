package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.ExpenseProduct;

public class ExpenseProductResponse {

    private Long id;
    private String name;
    private String unit;
    private int stockQuantity;
    private boolean lowStock;

    public static ExpenseProductResponse from(ExpenseProduct product) {
        ExpenseProductResponse dto = new ExpenseProductResponse();
        dto.id = product.getId();
        dto.name = product.getName();
        dto.unit = product.getUnit();
        dto.stockQuantity = product.getStockQuantity();
        dto.lowStock = product.getStockQuantity() <= 1;
        return dto;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getUnit() { return unit; }
    public int getStockQuantity() { return stockQuantity; }
    public boolean isLowStock() { return lowStock; }
}

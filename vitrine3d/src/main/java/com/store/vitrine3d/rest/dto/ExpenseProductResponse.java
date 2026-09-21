package com.store.vitrine3d.rest.dto;

import com.store.vitrine3d.domain.model.ExpenseProduct;
import com.store.vitrine3d.domain.model.MeasurementUnit;

import java.math.BigDecimal;

public class ExpenseProductResponse {
    private Long id;
    private String name;
    private String unit;
    private MeasurementUnit stockUnit;
    private BigDecimal stockQuantity;
    private BigDecimal lowStockAlert;
    private boolean lowStock;

    public static ExpenseProductResponse from(ExpenseProduct product) {
        ExpenseProductResponse dto = new ExpenseProductResponse();
        dto.id = product.getId();
        dto.name = product.getName();
        dto.unit = product.getUnit();
        dto.stockUnit = product.getStockUnit();
        dto.stockQuantity = product.getStockQuantity();
        dto.lowStockAlert = product.getLowStockAlert();
        dto.lowStock = product.isLowStock();
        return dto;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getUnit() { return unit; }
    public MeasurementUnit getStockUnit() { return stockUnit; }
    public BigDecimal getStockQuantity() { return stockQuantity; }
    public BigDecimal getLowStockAlert() { return lowStockAlert; }
    public boolean isLowStock() { return lowStock; }
}

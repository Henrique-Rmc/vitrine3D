package com.store.vitrine3d.domain.model;

import jakarta.persistence.*;

@Entity
@Table(name = "expense_products",
        uniqueConstraints = @UniqueConstraint(columnNames = {"store_id", "name"}))
public class ExpenseProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false)
    private String name;

    @Column(length = 50)
    private String unit;

    @Column(nullable = false)
    private int stockQuantity = 0;

    public ExpenseProduct() {}

    public Long getId() { return id; }

    public Store getStore() { return store; }
    public void setStore(Store store) { this.store = store; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
}

package com.store.vitrine3d.domain.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

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

    /** Rótulo livre de embalagem (ex.: "caixa", "pacote"). Não participa de cálculo. */
    @Column(length = 50)
    private String unit;

    /** Unidade em que o estoque deste material é medido — rege toda conversão. */
    @Enumerated(EnumType.STRING)
    @Column(name = "stock_unit", nullable = false, length = 10)
    private MeasurementUnit stockUnit = MeasurementUnit.UN;

    /** Sempre expresso em stockUnit. */
    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal stockQuantity = BigDecimal.ZERO;

    /**
     * Limite de alerta, em stockUnit. Alerta quando stockQuantity <= este valor.
     * Null = alerta não configurado, nunca dispara — distinto de zero, que significa
     * "avise quando o material zerar". Sem essa distinção, todo material recém-criado
     * nasceria pedindo reposição.
     */
    @Column(name = "low_stock_alert", precision = 14, scale = 3)
    private BigDecimal lowStockAlert;

    public ExpenseProduct() {}

    public Long getId() { return id; }

    public Store getStore() { return store; }
    public void setStore(Store store) { this.store = store; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public MeasurementUnit getStockUnit() { return stockUnit; }
    public void setStockUnit(MeasurementUnit stockUnit) { this.stockUnit = stockUnit; }

    public BigDecimal getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(BigDecimal stockQuantity) { this.stockQuantity = stockQuantity; }

    public BigDecimal getLowStockAlert() { return lowStockAlert; }
    public void setLowStockAlert(BigDecimal lowStockAlert) { this.lowStockAlert = lowStockAlert; }

    public boolean isLowStock() {
        return lowStockAlert != null && stockQuantity.compareTo(lowStockAlert) <= 0;
    }
}

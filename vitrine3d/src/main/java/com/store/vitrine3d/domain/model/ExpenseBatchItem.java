package com.store.vitrine3d.domain.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "expense_batch_items")
public class ExpenseBatchItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batch_id", nullable = false)
    private ExpenseBatch batch;

    @Column(name = "expense_product_id")
    private Long expenseProductId;

    @Column(nullable = false)
    private String productName;

    /** Rótulo livre de embalagem, snapshot do material. */
    @Column(length = 50)
    private String unit;

    /** Unidade de medida da quantidade comprada — convertida para a unidade do material. */
    @Enumerated(EnumType.STRING)
    @Column(name = "unit_code", nullable = false, length = 10)
    private MeasurementUnit unitCode = MeasurementUnit.UN;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal quantity = BigDecimal.ONE;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    public ExpenseBatchItem() {}

    public Long getId() { return id; }

    public ExpenseBatch getBatch() { return batch; }
    public void setBatch(ExpenseBatch batch) { this.batch = batch; }

    public Long getExpenseProductId() { return expenseProductId; }
    public void setExpenseProductId(Long expenseProductId) { this.expenseProductId = expenseProductId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public MeasurementUnit getUnitCode() { return unitCode; }
    public void setUnitCode(MeasurementUnit unitCode) { this.unitCode = unitCode; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}

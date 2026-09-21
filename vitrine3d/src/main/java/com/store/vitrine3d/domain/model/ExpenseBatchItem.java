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

    @Column(length = 50)
    private String unit;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private int quantity = 1;

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

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}

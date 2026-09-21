package com.store.vitrine3d.domain.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "expense_batches",
        uniqueConstraints = @UniqueConstraint(columnNames = {"store_id", "offline_id"}))
public class ExpenseBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(nullable = false, length = 36)
    private String offlineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id")
    private PdvEmployee operator;

    @Column(length = 500)
    private String note;

    @Column(nullable = false)
    private Instant batchDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant syncedAt;

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExpenseBatchItem> items = new ArrayList<>();

    public ExpenseBatch() {}

    public UUID getId() { return id; }

    public Store getStore() { return store; }
    public void setStore(Store store) { this.store = store; }

    public String getOfflineId() { return offlineId; }
    public void setOfflineId(String offlineId) { this.offlineId = offlineId; }

    public PdvEmployee getOperator() { return operator; }
    public void setOperator(PdvEmployee operator) { this.operator = operator; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Instant getBatchDate() { return batchDate; }
    public void setBatchDate(Instant batchDate) { this.batchDate = batchDate; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public Instant getSyncedAt() { return syncedAt; }

    public List<ExpenseBatchItem> getItems() { return items; }
    public void setItems(List<ExpenseBatchItem> items) { this.items = items; }
}

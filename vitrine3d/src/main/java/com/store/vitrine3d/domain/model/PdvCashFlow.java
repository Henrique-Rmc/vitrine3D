package com.store.vitrine3d.domain.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pdv_cash_flows",
        uniqueConstraints = @UniqueConstraint(columnNames = {"store_id", "offline_id"}))
public class PdvCashFlow {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 36)
    private String offlineId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id")
    private PdvEmployee operator;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlowType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FlowCategory category;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Instant flowDate;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant syncedAt;

    public PdvCashFlow() {}

    public UUID getId() { return id; }

    public String getOfflineId() { return offlineId; }
    public void setOfflineId(String offlineId) { this.offlineId = offlineId; }

    public Store getStore() { return store; }
    public void setStore(Store store) { this.store = store; }

    public PdvEmployee getOperator() { return operator; }
    public void setOperator(PdvEmployee operator) { this.operator = operator; }

    public FlowType getType() { return type; }
    public void setType(FlowType type) { this.type = type; }

    public FlowCategory getCategory() { return category; }
    public void setCategory(FlowCategory category) { this.category = category; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getFlowDate() { return flowDate; }
    public void setFlowDate(Instant flowDate) { this.flowDate = flowDate; }

    public Instant getSyncedAt() { return syncedAt; }
}

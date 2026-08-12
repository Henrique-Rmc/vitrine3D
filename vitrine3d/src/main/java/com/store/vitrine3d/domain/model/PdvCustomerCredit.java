package com.store.vitrine3d.domain.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pdv_customer_credits")
public class PdvCustomerCredit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private PdvCustomer customer;

    // Referência à venda de origem (sem FK para não bloquear cancelamento de venda)
    private UUID originSaleId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalDue;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    private Instant dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CreditStatus status = CreditStatus.OPEN;

    @Column(length = 500)
    private String note;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "credit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PdvCreditPayment> payments = new ArrayList<>();

    public PdvCustomerCredit() {}

    public UUID getId() { return id; }

    public PdvCustomer getCustomer() { return customer; }
    public void setCustomer(PdvCustomer customer) { this.customer = customer; }

    public UUID getOriginSaleId() { return originSaleId; }
    public void setOriginSaleId(UUID originSaleId) { this.originSaleId = originSaleId; }

    public BigDecimal getTotalDue() { return totalDue; }
    public void setTotalDue(BigDecimal totalDue) { this.totalDue = totalDue; }

    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }

    public Instant getDueDate() { return dueDate; }
    public void setDueDate(Instant dueDate) { this.dueDate = dueDate; }

    public CreditStatus getStatus() { return status; }
    public void setStatus(CreditStatus status) { this.status = status; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public List<PdvCreditPayment> getPayments() { return payments; }

    public BigDecimal getBalance() {
        return totalDue.subtract(amountPaid);
    }
}

package com.store.vitrine3d.domain.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "pdv_customer_relationships",
        uniqueConstraints = @UniqueConstraint(columnNames = {"customer_id", "relative_id"}))
public class PdvCustomerRelationship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private PdvCustomer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "relative_id", nullable = false)
    private PdvCustomer relative;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private FamilyRelationship relationship;

    @Column(length = 255)
    private String note;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public PdvCustomerRelationship() {}

    public Long getId() { return id; }

    public Store getStore() { return store; }
    public void setStore(Store store) { this.store = store; }

    public PdvCustomer getCustomer() { return customer; }
    public void setCustomer(PdvCustomer customer) { this.customer = customer; }

    public PdvCustomer getRelative() { return relative; }
    public void setRelative(PdvCustomer relative) { this.relative = relative; }

    public FamilyRelationship getRelationship() { return relationship; }
    public void setRelationship(FamilyRelationship relationship) { this.relationship = relationship; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Instant getCreatedAt() { return createdAt; }
}

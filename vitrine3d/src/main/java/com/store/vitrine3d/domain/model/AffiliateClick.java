package com.store.vitrine3d.domain.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "affiliate_clicks")
public class AffiliateClick {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant clickedAt;

    public AffiliateClick() {}

    public AffiliateClick(Product product) {
        this.product = product;
    }

    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public Instant getClickedAt() { return clickedAt; }
}

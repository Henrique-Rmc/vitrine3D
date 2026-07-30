package com.store.vitrine3d.domain.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "store_slug_history")
public class StoreSlugHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant replacedAt;

    public StoreSlugHistory() {}

    public StoreSlugHistory(String slug, Store store) {
        this.slug = slug;
        this.store = store;
    }

    public Long getId() { return id; }
    public String getSlug() { return slug; }
    public Store getStore() { return store; }
    public Instant getReplacedAt() { return replacedAt; }
}

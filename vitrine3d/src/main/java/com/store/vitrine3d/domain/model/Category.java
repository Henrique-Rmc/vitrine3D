package com.store.vitrine3d.domain.model;

import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Boolean isGlobal = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    public Category() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Boolean getIsGlobal() { return isGlobal; }
    public void setIsGlobal(Boolean isGlobal) { this.isGlobal = isGlobal; }

    public Store getStore() { return store; }
    public void setStore(Store store) { this.store = store; }
}

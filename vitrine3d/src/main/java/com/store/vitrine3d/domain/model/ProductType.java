package com.store.vitrine3d.domain.model;

import jakarta.persistence.*;

// Sub-categoria de produto criada pela propria loja (ex.: "Camisa", "Action Figure"),
// nunca global/curada pela plataforma — analoga aos atributos custom, so que um nivel
// acima. Permite que AttributeDefinition seja escopado a um subconjunto dos produtos
// da loja em vez de se aplicar a todos.
@Entity
@Table(name = "product_types",
        uniqueConstraints = @UniqueConstraint(columnNames = {"store_id", "product_type_key"}))
public class ProductType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "product_type_key", nullable = false)
    private String key;

    @Column(nullable = false)
    private String label;

    @Column(nullable = false)
    private Integer sortOrder = 0;

    public ProductType() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Store getStore() { return store; }
    public void setStore(Store store) { this.store = store; }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}

package com.store.vitrine3d.domain.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "attribute_definitions",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"store_id", "product_type_id", "attribute_key"}))
public class AttributeDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Todo atributo pertence a uma loja — nao existe mais atributo global/compartilhado
    // entre lojas (esse papel era do BusinessType, que virou so um rotulo de tracking).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    // Nulo = aplica a todos os produtos da loja. Preenchido = so aplica a produtos
    // daquele ProductType especifico.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_type_id")
    private ProductType productType;

    @Column(name = "attribute_key", nullable = false)
    private String key;

    @Column(nullable = false)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttributeType type;

    private String unit;

    @Column(nullable = false)
    private Boolean required = false;

    @Column(nullable = false)
    private Boolean filterable = true;

    @Column(nullable = false)
    private Integer sortOrder = 0;

    @ElementCollection
    @CollectionTable(name = "attribute_definition_options", joinColumns = @JoinColumn(name = "attribute_definition_id"))
    @Column(name = "option_value")
    @OrderColumn(name = "option_order")
    private List<String> enumOptions = new ArrayList<>();

    public AttributeDefinition() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Store getStore() { return store; }
    public void setStore(Store store) { this.store = store; }

    public ProductType getProductType() { return productType; }
    public void setProductType(ProductType productType) { this.productType = productType; }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public AttributeType getType() { return type; }
    public void setType(AttributeType type) { this.type = type; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public Boolean getRequired() { return required; }
    public void setRequired(Boolean required) { this.required = required; }

    public Boolean getFilterable() { return filterable; }
    public void setFilterable(Boolean filterable) { this.filterable = filterable; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public List<String> getEnumOptions() { return enumOptions; }
    public void setEnumOptions(List<String> enumOptions) { this.enumOptions = enumOptions; }
}

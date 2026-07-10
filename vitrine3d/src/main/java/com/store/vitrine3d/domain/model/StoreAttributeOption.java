package com.store.vitrine3d.domain.model;

import jakarta.persistence.*;

// Valores de um AttributeDefinition ENUM global que uma loja especifica cadastrou pra si.
// Existe pra nao vazar valores entre lojas da mesma vertical: um AttributeDefinition global
// (store == null) e compartilhado, entao os valores de cada loja nao podem viver nele.
// Atributos custom (AttributeDefinition.store != null) nao usam essa tabela — ja sao
// exclusivos de uma loja, entao continuam usando AttributeDefinition.enumOptions direto.
@Entity
@Table(name = "store_attribute_options",
        uniqueConstraints = @UniqueConstraint(columnNames = {"store_id", "attribute_definition_id", "option_value"}))
public class StoreAttributeOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_definition_id", nullable = false)
    private AttributeDefinition attributeDefinition;

    @Column(name = "option_value", nullable = false)
    private String value;

    @Column(name = "option_order", nullable = false)
    private Integer sortOrder = 0;

    public StoreAttributeOption() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Store getStore() { return store; }
    public void setStore(Store store) { this.store = store; }

    public AttributeDefinition getAttributeDefinition() { return attributeDefinition; }
    public void setAttributeDefinition(AttributeDefinition attributeDefinition) { this.attributeDefinition = attributeDefinition; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}

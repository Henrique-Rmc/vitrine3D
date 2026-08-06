package com.store.vitrine3d.domain.model;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // Bate com @Size(max = 2000) em ProductCreateRequest/ProductUpdateRequest — sem esse
    // @Column, o Hibernate gera varchar(255) por padrão, e uma descrição de 256-2000 chars
    // passa na validação da API mas quebra no INSERT com "value too long for type
    // character varying(255)", que cai no handler genérico e vira 500 sem mensagem útil.
    @Column(length = 2000)
    private String description;

    // length bate com @Size(max = 2048) no fallback imageUrls de ProductCreateRequest —
    // mesma classe de bug do description acima.
    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url", nullable = false, length = 2048)
    @OrderColumn(name = "image_order")
    private List<String> imageUrls = new ArrayList<>();

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Boolean isVisible = true;

    private Integer sortOrder;

    @Column(nullable = false)
    private Boolean featured = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    // Sub-categoria criada pela propria loja (ex.: "Camisa") — obrigatoria. Toda loja precisa
    // ter pelo menos um ProductType cadastrado antes de conseguir criar produtos.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_type_id", nullable = false)
    private ProductType productType;

    // Nullable no banco de propósito: ALTER TABLE ADD COLUMN ... NOT NULL falha em tabelas
    // com linhas existentes (sem valor pra preencher). Produtos novos sempre recebem pelo
    // menos um Map vazio via ProductAttributeValidator; produtos antigos ficam null.
    // Só relevante quando store.profileType == AFFILIATE.
    // Deve sempre começar com "https://" (validado no service).
    @Column(length = 2048)
    private String affiliateUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> attributes = new HashMap<>();

    public Product() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Boolean getIsVisible() { return isVisible; }
    public void setIsVisible(Boolean isVisible) { this.isVisible = isVisible; }

    public Boolean getFeatured() { return featured; }
    public void setFeatured(Boolean featured) { this.featured = featured; }

    public Store getStore() { return store; }
    public void setStore(Store store) { this.store = store; }

    public ProductType getProductType() { return productType; }
    public void setProductType(ProductType productType) { this.productType = productType; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public String getAffiliateUrl() { return affiliateUrl; }
    public void setAffiliateUrl(String affiliateUrl) { this.affiliateUrl = affiliateUrl; }

    public Map<String, Object> getAttributes() { return attributes; }
    public void setAttributes(Map<String, Object> attributes) { this.attributes = attributes; }
}

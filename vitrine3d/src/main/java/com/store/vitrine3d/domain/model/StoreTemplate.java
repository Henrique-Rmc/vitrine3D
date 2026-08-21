package com.store.vitrine3d.domain.model;

import jakarta.persistence.*;

@Entity
@Table(name = "store_templates")
public class StoreTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private LayoutMode layoutMode = LayoutMode.LOJA;

    @Column(length = 255)
    private String description;

    public StoreTemplate() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public LayoutMode getLayoutMode() { return layoutMode; }
    public void setLayoutMode(LayoutMode layoutMode) { this.layoutMode = layoutMode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

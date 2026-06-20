package com.store.vitrine3d.domain.model;

import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category extends StoreMetadata {
    public Category() {}
}

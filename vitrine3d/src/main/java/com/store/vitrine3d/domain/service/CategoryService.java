package com.store.vitrine3d.domain.service;

import com.store.vitrine3d.domain.model.Category;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    List<Category> findPublic();
    List<Category> findByStore(UUID storeId);
    Category findById(Long id);
    Category save(Category category);
    Category update(Long id, String name);
    void delete(Long id);
}

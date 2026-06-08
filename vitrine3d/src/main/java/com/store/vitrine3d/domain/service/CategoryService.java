package com.store.vitrine3d.domain.service;

import com.store.vitrine3d.domain.model.Category;

import java.util.List;

public interface CategoryService {
    List<Category> findAll();
    Category findById(Long id);
    Category save(Category category);
    void delete(Long id);
}

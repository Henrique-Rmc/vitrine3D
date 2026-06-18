package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByIsGlobalTrue();
    List<Category> findByIsGlobalTrueOrStoreId(UUID storeId);
}

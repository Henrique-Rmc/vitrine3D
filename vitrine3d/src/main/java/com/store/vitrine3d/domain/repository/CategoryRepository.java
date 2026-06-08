package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}

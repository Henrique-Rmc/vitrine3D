package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.StoreTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreTemplateRepository extends JpaRepository<StoreTemplate, Long> {
    boolean existsBySlug(String slug);
    Optional<StoreTemplate> findBySlug(String slug);
}

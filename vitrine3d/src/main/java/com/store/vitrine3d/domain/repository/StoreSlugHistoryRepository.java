package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.StoreSlugHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreSlugHistoryRepository extends JpaRepository<StoreSlugHistory, Long> {
    Optional<StoreSlugHistory> findBySlug(String slug);
    boolean existsBySlug(String slug);
}

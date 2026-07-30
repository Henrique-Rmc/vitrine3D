package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.StoreSlugHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface StoreSlugHistoryRepository extends JpaRepository<StoreSlugHistory, Long> {
    Optional<StoreSlugHistory> findBySlug(String slug);
    Optional<StoreSlugHistory> findBySlugAndStoreIsActiveTrue(String slug);
    boolean existsBySlug(String slug);

    @Modifying
    @Query("DELETE FROM StoreSlugHistory h WHERE h.store.id = :storeId")
    void deleteAllByStoreId(@Param("storeId") UUID storeId);
}

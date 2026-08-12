package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.PdvSale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PdvSaleRepository extends JpaRepository<PdvSale, UUID> {
    boolean existsByStoreIdAndOfflineId(UUID storeId, String offlineId);
    Optional<PdvSale> findByIdAndStoreId(UUID id, UUID storeId);
    Page<PdvSale> findByStoreIdAndSaleDateBetweenOrderBySaleDateDesc(UUID storeId, Instant from, Instant to, Pageable pageable);

    @Modifying
    @Query("DELETE FROM PdvSaleItem i WHERE i.sale.store.id = :storeId")
    void deleteItemsByStoreId(@Param("storeId") UUID storeId);

    @Modifying
    @Query("DELETE FROM PdvSale s WHERE s.store.id = :storeId")
    void deleteAllByStoreId(@Param("storeId") UUID storeId);
}

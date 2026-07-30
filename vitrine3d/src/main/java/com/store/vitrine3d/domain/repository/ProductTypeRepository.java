package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProductTypeRepository extends JpaRepository<ProductType, Long> {
    List<ProductType> findByStoreIdOrderBySortOrderAsc(UUID storeId);

    boolean existsByStoreIdAndKey(UUID storeId, String key);

    @Modifying
    @Query("DELETE FROM ProductType pt WHERE pt.store.id = :storeId")
    void deleteAllByStoreId(@Param("storeId") UUID storeId);
}

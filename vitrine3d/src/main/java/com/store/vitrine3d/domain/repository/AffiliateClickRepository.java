package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.AffiliateClick;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface AffiliateClickRepository extends JpaRepository<AffiliateClick, Long> {
    long countByProductId(Long productId);

    @Modifying
    @Query("DELETE FROM AffiliateClick ac WHERE ac.product.store.id = :storeId")
    void deleteAllByProductStoreId(@Param("storeId") UUID storeId);
}

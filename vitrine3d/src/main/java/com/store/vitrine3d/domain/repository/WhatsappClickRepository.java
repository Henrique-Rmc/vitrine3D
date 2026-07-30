package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.WhatsappClick;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface WhatsappClickRepository extends JpaRepository<WhatsappClick, Long> {
    long countByProductId(Long productId);

    @Modifying
    @Query("DELETE FROM WhatsappClick wc WHERE wc.product.store.id = :storeId")
    void deleteAllByProductStoreId(@Param("storeId") UUID storeId);
}

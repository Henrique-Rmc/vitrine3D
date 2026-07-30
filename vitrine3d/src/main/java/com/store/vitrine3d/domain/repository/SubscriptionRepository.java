package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByStoreId(UUID storeId);

    @Modifying
    @Query("DELETE FROM Subscription s WHERE s.store.id = :storeId")
    void deleteByStoreId(@Param("storeId") UUID storeId);
}

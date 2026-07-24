package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByStoreId(UUID storeId);
}

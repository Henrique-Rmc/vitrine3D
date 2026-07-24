package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.Order;
import com.store.vitrine3d.domain.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

// DORMANT — not wired into the system yet.
public interface OrderRepository extends JpaRepository<Order, UUID> {
    Page<Order> findByStoreId(UUID storeId, Pageable pageable);
    Page<Order> findByStoreIdAndStatus(UUID storeId, OrderStatus status, Pageable pageable);
    Optional<Order> findByIdAndStoreId(UUID id, UUID storeId);
    long countByStoreIdAndStatus(UUID storeId, OrderStatus status);
}

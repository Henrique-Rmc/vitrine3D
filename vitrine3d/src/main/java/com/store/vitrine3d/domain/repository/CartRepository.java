package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.Cart;
import com.store.vitrine3d.domain.model.CartStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

// DORMANT — not wired into the system yet.
public interface CartRepository extends JpaRepository<Cart, UUID> {
    Page<Cart> findByStoreIdAndStatus(UUID storeId, CartStatus status, Pageable pageable);
    Optional<Cart> findByIdAndStoreId(UUID id, UUID storeId);
}

package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.Cart;
import com.store.vitrine3d.domain.model.CartStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

// DORMANT — not wired into the system yet.
public interface CartRepository extends JpaRepository<Cart, UUID> {
    Page<Cart> findByStoreIdAndStatus(UUID storeId, CartStatus status, Pageable pageable);
    Optional<Cart> findByIdAndStoreId(UUID id, UUID storeId);

    @Modifying
    @Query(value = "DELETE FROM cart_items WHERE cart_id IN (SELECT id FROM carts WHERE store_id = :storeId)", nativeQuery = true)
    void deleteItemsByStoreId(@Param("storeId") UUID storeId);

    @Modifying
    @Query("DELETE FROM Cart c WHERE c.store.id = :storeId")
    void deleteAllByStoreId(@Param("storeId") UUID storeId);
}

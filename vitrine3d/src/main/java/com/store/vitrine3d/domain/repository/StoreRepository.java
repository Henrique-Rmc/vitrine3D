package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID>, JpaSpecificationExecutor<Store> {
    Optional<Store> findByEmail(String email);
    Optional<Store> findBySlug(String slug);
    Optional<Store> findBySlugAndIsActiveTrue(String slug);
    boolean existsByEmail(String email);
    boolean existsBySlug(String slug);
    Optional<Store> findByEmailVerificationToken(String token);
}

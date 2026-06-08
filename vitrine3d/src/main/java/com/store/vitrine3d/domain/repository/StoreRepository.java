package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {
    Optional<Store> findByEmail(String email);
    boolean existsByEmail(String email);
}

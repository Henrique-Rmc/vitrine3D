package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Page<Product> findByStoreId(UUID storeId, Pageable pageable);
    Page<Product> findByStoreIdAndIsVisibleTrue(UUID storeId, Pageable pageable);
    List<Product> findByStoreIdAndFeaturedTrue(UUID storeId);
    long countByStoreIdAndFeaturedTrue(UUID storeId);
}

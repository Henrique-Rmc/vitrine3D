package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByStoreId(Long storeId, Pageable pageable);
    Page<Product> findByStoreIdAndIsVisibleTrue(Long storeId, Pageable pageable);
    List<Product> findByStoreIdAndFeaturedTrue(Long storeId);
    long countByStoreIdAndFeaturedTrue(Long storeId);
}

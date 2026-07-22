package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductTypeRepository extends JpaRepository<ProductType, Long> {
    List<ProductType> findByStoreIdOrderBySortOrderAsc(UUID storeId);

    boolean existsByStoreIdAndKey(UUID storeId, String key);
}

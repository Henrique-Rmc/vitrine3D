package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.ExpenseProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExpenseProductRepository extends JpaRepository<ExpenseProduct, Long> {

    List<ExpenseProduct> findByStoreIdOrderByNameAsc(UUID storeId);

    List<ExpenseProduct> findByStoreIdAndNameContainingIgnoreCaseOrderByNameAsc(UUID storeId, String q);

    Optional<ExpenseProduct> findByStoreIdAndNameIgnoreCase(UUID storeId, String name);

    boolean existsByStoreIdAndNameIgnoreCase(UUID storeId, String name);

    List<ExpenseProduct> findByStoreIdAndStockQuantityLessThanEqualOrderByNameAsc(UUID storeId, int threshold);
}

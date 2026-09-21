package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.ExpenseProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExpenseProductRepository extends JpaRepository<ExpenseProduct, Long> {

    List<ExpenseProduct> findByStoreIdOrderByNameAsc(UUID storeId);

    List<ExpenseProduct> findByStoreIdAndNameContainingIgnoreCaseOrderByNameAsc(UUID storeId, String q);

    Optional<ExpenseProduct> findByStoreIdAndNameIgnoreCase(UUID storeId, String name);

    boolean existsByStoreIdAndNameIgnoreCase(UUID storeId, String name);

    /** Alerta por material: cada um tem seu próprio limite, em vez de um valor fixo global. */
    @Query("SELECT p FROM ExpenseProduct p WHERE p.store.id = :storeId "
         + "AND p.stockQuantity <= p.lowStockAlert ORDER BY p.name ASC")
    List<ExpenseProduct> findLowStock(@Param("storeId") UUID storeId);
}

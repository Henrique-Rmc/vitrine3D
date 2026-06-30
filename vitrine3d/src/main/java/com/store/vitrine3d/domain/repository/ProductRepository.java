package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Page<Product> findByStoreId(UUID storeId, Pageable pageable);
    Page<Product> findByStoreIdAndIsVisibleTrueAndStoreIsActiveTrue(UUID storeId, Pageable pageable);
    List<Product> findByStoreIdAndFeaturedTrueAndStoreIsActiveTrue(UUID storeId);
    List<Product> findByStoreId(UUID storeId);
    long countByStoreIdAndFeaturedTrue(UUID storeId);

    @Modifying
    @Query(value = """
            UPDATE products SET sort_order = v.ord
            FROM (
                SELECT unnest(cast(:ids AS bigint[])) AS id,
                       generate_series(0, array_length(cast(:ids AS bigint[]), 1) - 1) AS ord
            ) v
            WHERE products.id = v.id AND products.store_id = cast(:storeId AS uuid)
            """, nativeQuery = true)
    void bulkUpdateSortOrder(@Param("ids") String ids, @Param("storeId") String storeId);
}

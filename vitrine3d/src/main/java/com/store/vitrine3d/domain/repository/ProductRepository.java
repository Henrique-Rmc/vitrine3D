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

    @Modifying
    @Query(value = "DELETE FROM product_images WHERE product_id IN (SELECT id FROM products WHERE store_id = :storeId)", nativeQuery = true)
    void deleteImagesByStoreId(@Param("storeId") UUID storeId);

    /**
     * Reescreve o valor de um atributo ENUM (Product.attributes, jsonb) em todo produto da loja
     * que ainda referencia o valor antigo de uma opcao renomeada — sem isso, o produto ficaria
     * com um valor orfao que nao bate mais com nenhuma opcao valida do AttributeDefinition.
     * productTypeId nulo = atributo geral, aplica a todos os produtos da loja; preenchido =
     * so aos produtos daquele ProductType (mesma semantica de AttributeDefinition.productType).
     */
    @Modifying
    @Query(value = """
            UPDATE products
            SET attributes = jsonb_set(attributes, ('{' || :key || '}')::text[], to_jsonb(cast(:newValue as text)))
            WHERE store_id = :storeId
              AND (:productTypeId IS NULL OR product_type_id = :productTypeId)
              AND attributes ->> :key = :oldValue
            """, nativeQuery = true)
    void renameAttributeOptionValue(@Param("storeId") UUID storeId,
                                     @Param("productTypeId") Long productTypeId,
                                     @Param("key") String key,
                                     @Param("oldValue") String oldValue,
                                     @Param("newValue") String newValue);

    @Modifying
    @Query("DELETE FROM Product p WHERE p.store.id = :storeId")
    void deleteAllByStoreId(@Param("storeId") UUID storeId);
}

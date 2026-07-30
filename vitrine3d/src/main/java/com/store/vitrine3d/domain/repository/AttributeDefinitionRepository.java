package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.AttributeDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AttributeDefinitionRepository extends JpaRepository<AttributeDefinition, Long> {
    /**
     * Atributos efetivamente disponiveis para uma loja, opcionalmente escopados a um
     * ProductType: gerais da loja (productType IS NULL) + escopados a esse ProductType
     * especifico. productTypeId pode ser nulo — nesse caso "d.productType.id =
     * :productTypeId" nunca bate (logica de tres valores do SQL), entao so o ramo
     * "d.productType IS NULL" casa, que e exatamente o comportamento de produto sem tipo.
     */
    @Query("SELECT d FROM AttributeDefinition d WHERE d.store.id = :storeId " +
           "AND (d.productType IS NULL OR d.productType.id = :productTypeId) " +
           "ORDER BY d.sortOrder ASC")
    List<AttributeDefinition> findEffectiveForStore(@Param("storeId") UUID storeId,
                                                     @Param("productTypeId") Long productTypeId);

    /** Todos os atributos de uma loja, gerais ou escopados a qualquer ProductType — usado pra checar colisao de key na criacao. */
    List<AttributeDefinition> findByStoreId(UUID storeId);

    @Modifying
    @Query(value = "DELETE FROM attribute_definition_options WHERE attribute_definition_id IN (SELECT id FROM attribute_definitions WHERE store_id = :storeId)", nativeQuery = true)
    void deleteOptionsByStoreId(@Param("storeId") UUID storeId);

    @Modifying
    @Query("DELETE FROM AttributeDefinition ad WHERE ad.store.id = :storeId")
    void deleteAllByStoreId(@Param("storeId") UUID storeId);
}

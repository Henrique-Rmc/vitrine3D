package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.AttributeDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AttributeDefinitionRepository extends JpaRepository<AttributeDefinition, Long> {
    /** Schema "puro" da vertical, sem contexto de loja — usado pelo cadastro/BusinessTypeController. */
    List<AttributeDefinition> findByBusinessTypeIdOrderBySortOrderAsc(Long businessTypeId);

    /**
     * Atributos efetivamente disponiveis para uma loja: globais da vertical (store IS NULL)
     * + customizados dela mesma. Nome derivado do Spring Data nao expressa corretamente
     * "X AND (A OR B)", por isso a query explicita.
     */
    @Query("SELECT d FROM AttributeDefinition d WHERE d.businessType.id = :businessTypeId " +
           "AND (d.store IS NULL OR d.store.id = :storeId) ORDER BY d.sortOrder ASC")
    List<AttributeDefinition> findEffectiveForStore(@Param("businessTypeId") Long businessTypeId,
                                                     @Param("storeId") UUID storeId);
}

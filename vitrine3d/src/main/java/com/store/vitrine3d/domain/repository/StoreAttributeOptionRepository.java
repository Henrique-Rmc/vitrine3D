package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.StoreAttributeOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StoreAttributeOptionRepository extends JpaRepository<StoreAttributeOption, Long> {
    List<StoreAttributeOption> findByStoreIdAndAttributeDefinitionIdOrderBySortOrderAsc(
            UUID storeId, Long attributeDefinitionId);

    boolean existsByStoreIdAndAttributeDefinitionIdAndValue(
            UUID storeId, Long attributeDefinitionId, String value);

    long countByStoreIdAndAttributeDefinitionId(UUID storeId, Long attributeDefinitionId);
}

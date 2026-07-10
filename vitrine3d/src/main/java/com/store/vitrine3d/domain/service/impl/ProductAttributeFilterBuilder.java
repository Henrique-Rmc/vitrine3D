package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.AttributeDefinition;
import com.store.vitrine3d.domain.model.BusinessType;
import com.store.vitrine3d.domain.model.Product;
import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.repository.AttributeDefinitionRepository;
import com.store.vitrine3d.domain.repository.StoreRepository;
import com.store.vitrine3d.domain.service.AttributeTypeHandler;
import com.store.vitrine3d.domain.service.AttributeTypeHandlerRegistry;
import com.store.vitrine3d.rest.exception.BusinessRuleException;
import com.store.vitrine3d.rest.exception.ResourceNotFoundException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Traduz o Map<String,String> dinamico de ProductFilter.attributes em predicados JSONB
 * (ProductSpec), delegando a interpretacao de cada tipo ao AttributeTypeHandler correspondente.
 *
 * Convencao de chave: "marca" = igualdade; "ano_min"/"ano_max" = faixa (NUMBER/DATE).
 */
@Component
public class ProductAttributeFilterBuilder {

    private static final String MIN_SUFFIX = "_min";
    private static final String MAX_SUFFIX = "_max";

    private final StoreRepository storeRepository;
    private final AttributeDefinitionRepository attributeDefinitionRepository;
    private final AttributeTypeHandlerRegistry handlerRegistry;

    public ProductAttributeFilterBuilder(StoreRepository storeRepository,
                                          AttributeDefinitionRepository attributeDefinitionRepository,
                                          AttributeTypeHandlerRegistry handlerRegistry) {
        this.storeRepository = storeRepository;
        this.attributeDefinitionRepository = attributeDefinitionRepository;
        this.handlerRegistry = handlerRegistry;
    }

    public List<Specification<Product>> build(UUID storeId, Map<String, String> rawFilters) {
        if (rawFilters == null || rawFilters.isEmpty()) {
            return List.of();
        }

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", storeId));
        BusinessType businessType = store.getBusinessType();
        if (businessType == null) {
            throw new BusinessRuleException("BUSINESS_TYPE_REQUIRED",
                    "This store has no business type; attribute filters are not available.");
        }

        Map<String, AttributeDefinition> definitionsByKey = attributeDefinitionRepository
                .findEffectiveForStore(businessType.getId(), store.getId()).stream()
                .collect(Collectors.toMap(AttributeDefinition::getKey, Function.identity()));

        List<Specification<Product>> specs = new ArrayList<>();
        Map<String, String[]> ranges = new HashMap<>();

        for (Map.Entry<String, String> entry : rawFilters.entrySet()) {
            String value = entry.getValue();
            if (value == null || value.isBlank()) continue;

            boolean isMin = entry.getKey().endsWith(MIN_SUFFIX);
            boolean isMax = entry.getKey().endsWith(MAX_SUFFIX);
            String baseKey = isMin ? stripSuffix(entry.getKey(), MIN_SUFFIX)
                    : isMax ? stripSuffix(entry.getKey(), MAX_SUFFIX)
                    : entry.getKey();

            AttributeDefinition definition = requireFilterableDefinition(definitionsByKey, businessType, baseKey);

            if (isMin || isMax) {
                String[] minMax = ranges.computeIfAbsent(baseKey, k -> new String[2]);
                minMax[isMin ? 0 : 1] = value;
            } else {
                AttributeTypeHandler handler = handlerRegistry.resolve(definition.getType());
                specs.add(handler.equalsFilter(baseKey, value));
            }
        }

        for (Map.Entry<String, String[]> entry : ranges.entrySet()) {
            AttributeDefinition definition = definitionsByKey.get(entry.getKey());
            AttributeTypeHandler handler = handlerRegistry.resolve(definition.getType());
            specs.add(handler.betweenFilter(entry.getKey(), entry.getValue()[0], entry.getValue()[1]));
        }

        return specs;
    }

    private AttributeDefinition requireFilterableDefinition(Map<String, AttributeDefinition> definitionsByKey,
                                                              BusinessType businessType, String key) {
        AttributeDefinition definition = definitionsByKey.get(key);
        if (definition == null) {
            throw new BusinessRuleException("UNKNOWN_ATTRIBUTE",
                    "Attribute '" + key + "' is not defined for business type '" + businessType.getName() + "'.");
        }
        if (!Boolean.TRUE.equals(definition.getFilterable())) {
            throw new BusinessRuleException("ATTRIBUTE_NOT_FILTERABLE",
                    "Attribute '" + key + "' is not filterable.");
        }
        return definition;
    }

    private String stripSuffix(String value, String suffix) {
        return value.substring(0, value.length() - suffix.length());
    }
}

package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.AttributeDefinition;
import com.store.vitrine3d.domain.model.AttributeType;
import com.store.vitrine3d.domain.model.BusinessType;
import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.model.StoreAttributeOption;
import com.store.vitrine3d.domain.repository.AttributeDefinitionRepository;
import com.store.vitrine3d.domain.repository.ProductRepository;
import com.store.vitrine3d.domain.repository.StoreAttributeOptionRepository;
import com.store.vitrine3d.domain.repository.StoreRepository;
import com.store.vitrine3d.domain.service.CurrentStoreResolver;
import com.store.vitrine3d.domain.specification.ProductSpec;
import com.store.vitrine3d.rest.dto.AttributeDefinitionCreateRequest;
import com.store.vitrine3d.rest.exception.BusinessRuleException;
import com.store.vitrine3d.rest.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Gerencia atributos customizados por loja (criacao/exclusao) e o opt-out de atributos
 * globais (esconder sem apagar). A validacao/filtro de produto continuam em
 * ProductAttributeValidator/ProductAttributeFilterBuilder, que passam a enxergar o
 * resultado dessas operacoes via AttributeDefinitionRepository.findEffectiveForStore.
 */
@Service
@Transactional
public class StoreAttributeDefinitionService {

    private final AttributeDefinitionRepository attributeDefinitionRepository;
    private final StoreAttributeOptionRepository storeAttributeOptionRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final CurrentStoreResolver currentStoreResolver;
    private final EffectiveAttributeDefinitionResolver effectiveAttributeDefinitionResolver;

    public StoreAttributeDefinitionService(AttributeDefinitionRepository attributeDefinitionRepository,
                                            StoreAttributeOptionRepository storeAttributeOptionRepository,
                                            ProductRepository productRepository,
                                            StoreRepository storeRepository,
                                            CurrentStoreResolver currentStoreResolver,
                                            EffectiveAttributeDefinitionResolver effectiveAttributeDefinitionResolver) {
        this.attributeDefinitionRepository = attributeDefinitionRepository;
        this.storeAttributeOptionRepository = storeAttributeOptionRepository;
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
        this.currentStoreResolver = currentStoreResolver;
        this.effectiveAttributeDefinitionResolver = effectiveAttributeDefinitionResolver;
    }

    /** Publico — usado tanto pelo formulario de produto do lojista quanto pelo filtro da vitrine. */
    @Transactional(readOnly = true)
    public List<AttributeDefinition> listEffective(UUID storeId) {
        Store store = findStore(storeId);
        BusinessType businessType = store.getBusinessType();
        if (businessType == null) {
            return List.of();
        }
        return attributeDefinitionRepository.findEffectiveForStore(businessType.getId(), store.getId()).stream()
                .filter(definition -> !store.getHiddenAttributeDefinitionIds().contains(definition.getId()))
                .map(definition -> effectiveAttributeDefinitionResolver.withEffectiveOptions(store, definition))
                .toList();
    }

    /** Cadastra um valor de opcao pra um atributo ENUM (global da vertical ou custom da propria loja). */
    public AttributeDefinition addOption(UUID storeId, Long attributeDefinitionId, String value) {
        Store store = findStore(storeId);
        assertStoreOwnership(store);

        AttributeDefinition definition = attributeDefinitionRepository.findById(attributeDefinitionId)
                .orElseThrow(() -> new ResourceNotFoundException("AttributeDefinition", attributeDefinitionId));

        if (definition.getType() != AttributeType.ENUM) {
            throw new BusinessRuleException("NOT_ENUM_ATTRIBUTE",
                    "Only ENUM attributes accept registered option values.");
        }

        boolean isOwnCustom = definition.getStore() != null && definition.getStore().getId().equals(store.getId());
        boolean isGlobalForBusinessType = definition.getStore() == null
                && store.getBusinessType() != null
                && definition.getBusinessType().getId().equals(store.getBusinessType().getId());
        if (!isOwnCustom && !isGlobalForBusinessType) {
            throw new AccessDeniedException("This attribute is not available to your store.");
        }

        String trimmed = value != null ? value.trim() : "";
        if (trimmed.isEmpty()) {
            throw new BusinessRuleException("INVALID_OPTION_VALUE", "Option value must not be blank.");
        }

        if (isOwnCustom) {
            if (definition.getEnumOptions().contains(trimmed)) {
                throw new BusinessRuleException("OPTION_ALREADY_EXISTS",
                        "Option '" + trimmed + "' already exists for this attribute.");
            }
            definition.getEnumOptions().add(trimmed);
            attributeDefinitionRepository.save(definition);
        } else {
            if (storeAttributeOptionRepository.existsByStoreIdAndAttributeDefinitionIdAndValue(
                    store.getId(), definition.getId(), trimmed)) {
                throw new BusinessRuleException("OPTION_ALREADY_EXISTS",
                        "Option '" + trimmed + "' already exists for this attribute.");
            }
            int nextOrder = (int) storeAttributeOptionRepository
                    .countByStoreIdAndAttributeDefinitionId(store.getId(), definition.getId());
            StoreAttributeOption option = new StoreAttributeOption();
            option.setStore(store);
            option.setAttributeDefinition(definition);
            option.setValue(trimmed);
            option.setSortOrder(nextOrder);
            storeAttributeOptionRepository.save(option);
        }

        return effectiveAttributeDefinitionResolver.withEffectiveOptions(store, definition);
    }

    public AttributeDefinition createCustom(UUID storeId, AttributeDefinitionCreateRequest request) {
        Store store = findStore(storeId);
        assertStoreOwnership(store);

        BusinessType businessType = store.getBusinessType();
        if (businessType == null) {
            throw new BusinessRuleException("BUSINESS_TYPE_REQUIRED",
                    "Store must have a business type before creating custom attributes.");
        }

        List<AttributeDefinition> effective = attributeDefinitionRepository
                .findEffectiveForStore(businessType.getId(), store.getId());
        boolean keyTaken = effective.stream().anyMatch(d -> d.getKey().equals(request.getKey()));
        if (keyTaken) {
            throw new BusinessRuleException("ATTRIBUTE_KEY_TAKEN",
                    "Attribute key '" + request.getKey() + "' is already in use for this business type.");
        }

        AttributeDefinition definition = new AttributeDefinition();
        definition.setBusinessType(businessType);
        definition.setStore(store);
        definition.setKey(request.getKey());
        definition.setLabel(request.getLabel());
        definition.setType(request.getType());
        definition.setUnit(request.getUnit());
        definition.setRequired(Boolean.TRUE.equals(request.getRequired()));
        definition.setFilterable(request.getFilterable() == null || request.getFilterable());
        definition.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 100);
        if (request.getEnumOptions() != null) {
            definition.setEnumOptions(new ArrayList<>(request.getEnumOptions()));
        }
        return attributeDefinitionRepository.save(definition);
    }

    public void deleteCustom(UUID storeId, Long attributeDefinitionId) {
        Store store = findStore(storeId);
        assertStoreOwnership(store);

        AttributeDefinition definition = requireOwnCustomAttribute(store, attributeDefinitionId);

        boolean inUse = productRepository.exists(
                ProductSpec.fromStore(store.getId()).and(ProductSpec.hasAttributeKey(definition.getKey())));
        if (inUse) {
            throw new BusinessRuleException("ATTRIBUTE_IN_USE",
                    "Cannot delete attribute '" + definition.getKey()
                            + "' — it is used by existing products. Remove it from those products first.");
        }

        attributeDefinitionRepository.delete(definition);
    }

    public void hideGlobal(UUID storeId, Long attributeDefinitionId) {
        Store store = findStore(storeId);
        assertStoreOwnership(store);

        AttributeDefinition definition = requireOwnGlobalAttribute(store, attributeDefinitionId);
        store.getHiddenAttributeDefinitionIds().add(definition.getId());
        storeRepository.save(store);
    }

    public void unhideGlobal(UUID storeId, Long attributeDefinitionId) {
        Store store = findStore(storeId);
        assertStoreOwnership(store);

        AttributeDefinition definition = requireOwnGlobalAttribute(store, attributeDefinitionId);
        store.getHiddenAttributeDefinitionIds().remove(definition.getId());
        storeRepository.save(store);
    }

    private Store findStore(UUID storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", storeId));
    }

    private void assertStoreOwnership(Store store) {
        Store current = currentStoreResolver.getCurrentStore();
        if (!store.getId().equals(current.getId())) {
            throw new AccessDeniedException("You do not have permission to manage attributes for this store.");
        }
    }

    private AttributeDefinition requireOwnCustomAttribute(Store store, Long attributeDefinitionId) {
        AttributeDefinition definition = attributeDefinitionRepository.findById(attributeDefinitionId)
                .orElseThrow(() -> new ResourceNotFoundException("AttributeDefinition", attributeDefinitionId));
        if (definition.getStore() == null || !definition.getStore().getId().equals(store.getId())) {
            throw new AccessDeniedException("You can only manage your own custom attributes.");
        }
        return definition;
    }

    private AttributeDefinition requireOwnGlobalAttribute(Store store, Long attributeDefinitionId) {
        AttributeDefinition definition = attributeDefinitionRepository.findById(attributeDefinitionId)
                .orElseThrow(() -> new ResourceNotFoundException("AttributeDefinition", attributeDefinitionId));
        if (definition.getStore() != null) {
            throw new BusinessRuleException("NOT_A_GLOBAL_ATTRIBUTE",
                    "Only global attributes can be hidden — delete your own custom attributes instead.");
        }
        BusinessType storeBusinessType = store.getBusinessType();
        if (storeBusinessType == null || !definition.getBusinessType().getId().equals(storeBusinessType.getId())) {
            throw new AccessDeniedException("This attribute does not belong to your business type.");
        }
        return definition;
    }
}

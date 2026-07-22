package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.AttributeDefinition;
import com.store.vitrine3d.domain.model.AttributeType;
import com.store.vitrine3d.domain.model.ProductType;
import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.repository.AttributeDefinitionRepository;
import com.store.vitrine3d.domain.repository.ProductRepository;
import com.store.vitrine3d.domain.repository.ProductTypeRepository;
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
 * Gerencia atributos customizados por loja (criacao/exclusao/cadastro de valor de opcao).
 * Todo atributo pertence exclusivamente a uma loja — nao existe mais atributo global
 * compartilhado entre lojas. A validacao/filtro de produto continuam em
 * ProductAttributeValidator/ProductAttributeFilterBuilder, que enxergam o resultado dessas
 * operacoes via AttributeDefinitionRepository.findEffectiveForStore.
 */
@Service
@Transactional
public class StoreAttributeDefinitionService {

    private final AttributeDefinitionRepository attributeDefinitionRepository;
    private final ProductRepository productRepository;
    private final ProductTypeRepository productTypeRepository;
    private final StoreRepository storeRepository;
    private final CurrentStoreResolver currentStoreResolver;

    public StoreAttributeDefinitionService(AttributeDefinitionRepository attributeDefinitionRepository,
                                            ProductRepository productRepository,
                                            ProductTypeRepository productTypeRepository,
                                            StoreRepository storeRepository,
                                            CurrentStoreResolver currentStoreResolver) {
        this.attributeDefinitionRepository = attributeDefinitionRepository;
        this.productRepository = productRepository;
        this.productTypeRepository = productTypeRepository;
        this.storeRepository = storeRepository;
        this.currentStoreResolver = currentStoreResolver;
    }

    /** Publico — usado tanto pelo formulario de produto do lojista quanto pelo filtro da vitrine. */
    @Transactional(readOnly = true)
    public List<AttributeDefinition> listEffective(UUID storeId, Long productTypeId) {
        return attributeDefinitionRepository.findEffectiveForStore(storeId, productTypeId);
    }

    /** Cadastra um valor de opcao pra um atributo ENUM da propria loja. */
    public AttributeDefinition addOption(UUID storeId, Long attributeDefinitionId, String value) {
        Store store = findStore(storeId);
        assertStoreOwnership(store);

        AttributeDefinition definition = requireOwnAttribute(store, attributeDefinitionId);

        if (definition.getType() != AttributeType.ENUM) {
            throw new BusinessRuleException("NOT_ENUM_ATTRIBUTE",
                    "Only ENUM attributes accept registered option values.");
        }

        String trimmed = value != null ? value.trim() : "";
        if (trimmed.isEmpty()) {
            throw new BusinessRuleException("INVALID_OPTION_VALUE", "Option value must not be blank.");
        }

        if (definition.getEnumOptions().contains(trimmed)) {
            throw new BusinessRuleException("OPTION_ALREADY_EXISTS",
                    "Option '" + trimmed + "' already exists for this attribute.");
        }
        definition.getEnumOptions().add(trimmed);
        return attributeDefinitionRepository.save(definition);
    }

    public AttributeDefinition createCustom(UUID storeId, AttributeDefinitionCreateRequest request) {
        Store store = findStore(storeId);
        assertStoreOwnership(store);

        ProductType productType = null;
        if (request.getProductTypeId() != null) {
            productType = productTypeRepository.findById(request.getProductTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("ProductType", request.getProductTypeId()));
            if (!productType.getStore().getId().equals(store.getId())) {
                throw new AccessDeniedException("This product type does not belong to your store.");
            }
        }

        assertKeyAvailable(store, productType, request.getKey());

        AttributeDefinition definition = new AttributeDefinition();
        definition.setStore(store);
        definition.setProductType(productType);
        definition.setKey(request.getKey());
        definition.setLabel(request.getLabel());
        definition.setType(AttributeType.ENUM);
        definition.setRequired(Boolean.TRUE.equals(request.getRequired()));
        definition.setFilterable(request.getFilterable() == null || request.getFilterable());
        definition.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 100);
        if (request.getEnumOptions() != null) {
            definition.setEnumOptions(new ArrayList<>(request.getEnumOptions()));
        }
        return attributeDefinitionRepository.save(definition);
    }

    /**
     * Um atributo escopado a um ProductType colide com atributos gerais da loja (co-resolvem
     * juntos pra qualquer produto daquele tipo) ou outro ja escopado ao mesmo tipo. Um
     * atributo geral colide com QUALQUER atributo que a loja ja tenha — geral ou escopado a
     * qualquer tipo — porque um atributo geral co-resolve com todos os ProductTypes da loja.
     */
    private void assertKeyAvailable(Store store, ProductType productType, String key) {
        Long productTypeId = productType != null ? productType.getId() : null;
        List<AttributeDefinition> candidates = new ArrayList<>(
                attributeDefinitionRepository.findEffectiveForStore(store.getId(), productTypeId));
        if (productType == null) {
            candidates.addAll(attributeDefinitionRepository.findByStoreId(store.getId()));
        }

        boolean keyTaken = candidates.stream().anyMatch(d -> d.getKey().equals(key));
        if (keyTaken) {
            throw new BusinessRuleException("ATTRIBUTE_KEY_TAKEN",
                    "Attribute key '" + key + "' is already in use for this store.");
        }
    }

    public void deleteCustom(UUID storeId, Long attributeDefinitionId) {
        Store store = findStore(storeId);
        assertStoreOwnership(store);

        AttributeDefinition definition = requireOwnAttribute(store, attributeDefinitionId);

        boolean inUse = productRepository.exists(
                ProductSpec.fromStore(store.getId()).and(ProductSpec.hasAttributeKey(definition.getKey())));
        if (inUse) {
            throw new BusinessRuleException("ATTRIBUTE_IN_USE",
                    "Cannot delete attribute '" + definition.getKey()
                            + "' — it is used by existing products. Remove it from those products first.");
        }

        attributeDefinitionRepository.delete(definition);
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

    private AttributeDefinition requireOwnAttribute(Store store, Long attributeDefinitionId) {
        AttributeDefinition definition = attributeDefinitionRepository.findById(attributeDefinitionId)
                .orElseThrow(() -> new ResourceNotFoundException("AttributeDefinition", attributeDefinitionId));
        if (!definition.getStore().getId().equals(store.getId())) {
            throw new AccessDeniedException("You can only manage your own attributes.");
        }
        return definition;
    }
}

package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.AttributeDefinition;
import com.store.vitrine3d.domain.model.ProductType;
import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.repository.AttributeDefinitionRepository;
import com.store.vitrine3d.domain.repository.ProductRepository;
import com.store.vitrine3d.domain.repository.ProductTypeRepository;
import com.store.vitrine3d.domain.repository.StoreRepository;
import com.store.vitrine3d.domain.service.CurrentStoreResolver;
import com.store.vitrine3d.domain.specification.ProductSpec;
import com.store.vitrine3d.rest.dto.ProductTypeCreateRequest;
import com.store.vitrine3d.rest.exception.BusinessRuleException;
import com.store.vitrine3d.rest.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Sub-categorias de produto criadas pela propria loja (ex.: "Camisa", "Action Figure"),
 * nunca curadas pela plataforma — analogas a atributos custom, um nivel acima. Permitem
 * escopar AttributeDefinition a um subconjunto dos produtos da loja em vez de todos.
 */
@Service
@Transactional
public class ProductTypeService {

    private final ProductTypeRepository productTypeRepository;
    private final AttributeDefinitionRepository attributeDefinitionRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final CurrentStoreResolver currentStoreResolver;

    public ProductTypeService(ProductTypeRepository productTypeRepository,
                               AttributeDefinitionRepository attributeDefinitionRepository,
                               ProductRepository productRepository,
                               StoreRepository storeRepository,
                               CurrentStoreResolver currentStoreResolver) {
        this.productTypeRepository = productTypeRepository;
        this.attributeDefinitionRepository = attributeDefinitionRepository;
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
        this.currentStoreResolver = currentStoreResolver;
    }

    @Transactional(readOnly = true)
    public List<ProductType> listByStore(UUID storeId) {
        return productTypeRepository.findByStoreIdOrderBySortOrderAsc(storeId);
    }

    public ProductType create(UUID storeId, ProductTypeCreateRequest request) {
        Store store = findStore(storeId);
        assertStoreOwnership(store);

        if (productTypeRepository.existsByStoreIdAndKey(storeId, request.getKey())) {
            throw new BusinessRuleException("PRODUCT_TYPE_KEY_TAKEN",
                    "Product type key '" + request.getKey() + "' is already in use for this store.");
        }

        ProductType productType = new ProductType();
        productType.setStore(store);
        productType.setKey(request.getKey());
        productType.setLabel(request.getLabel());
        productType.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        return productTypeRepository.save(productType);
    }

    public void delete(UUID storeId, Long productTypeId) {
        Store store = findStore(storeId);
        assertStoreOwnership(store);

        ProductType productType = productTypeRepository.findById(productTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductType", productTypeId));
        if (!productType.getStore().getId().equals(store.getId())) {
            throw new AccessDeniedException("You can only manage your own product types.");
        }

        boolean inUse = productRepository.exists(
                ProductSpec.fromStore(store.getId()).and(ProductSpec.hasProductType(productTypeId)));
        if (inUse) {
            throw new BusinessRuleException("PRODUCT_TYPE_IN_USE",
                    "Cannot delete product type '" + productType.getLabel()
                            + "' — it is used by existing products. Remove it from those products first.");
        }

        List<AttributeDefinition> scoped = attributeDefinitionRepository.findByStoreId(store.getId()).stream()
                .filter(definition -> definition.getProductType() != null
                        && definition.getProductType().getId().equals(productTypeId))
                .toList();
        attributeDefinitionRepository.deleteAll(scoped);

        productTypeRepository.delete(productType);
    }

    private Store findStore(UUID storeId) {
        return storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", storeId));
    }

    private void assertStoreOwnership(Store store) {
        Store current = currentStoreResolver.getCurrentStore();
        if (!store.getId().equals(current.getId())) {
            throw new AccessDeniedException("You do not have permission to manage product types for this store.");
        }
    }
}

package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.ProductType;
import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.repository.AttributeDefinitionRepository;
import com.store.vitrine3d.domain.repository.ProductRepository;
import com.store.vitrine3d.domain.repository.ProductTypeRepository;
import com.store.vitrine3d.domain.repository.StoreRepository;
import com.store.vitrine3d.domain.service.CurrentStoreResolver;
import com.store.vitrine3d.rest.dto.ProductTypeUpdateRequest;
import com.store.vitrine3d.rest.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductTypeServiceTest {

    @Mock private ProductTypeRepository productTypeRepository;
    @Mock private AttributeDefinitionRepository attributeDefinitionRepository;
    @Mock private ProductRepository productRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private CurrentStoreResolver currentStoreResolver;

    private ProductTypeService service;

    private static final UUID STORE_ID = UUID.randomUUID();

    private Store store;

    @BeforeEach
    void setUp() {
        service = new ProductTypeService(
                productTypeRepository, attributeDefinitionRepository, productRepository, storeRepository, currentStoreResolver);

        store = new Store();
        store.setId(STORE_ID);

        lenient().when(storeRepository.findById(STORE_ID)).thenReturn(Optional.of(store));
    }

    private ProductType productType(Long id, Store owner) {
        ProductType productType = new ProductType();
        productType.setId(id);
        productType.setStore(owner);
        productType.setKey("perfumes");
        productType.setLabel("Perfumes Masculino-Feminino");
        return productType;
    }

    private ProductTypeUpdateRequest updateRequest(String label) {
        ProductTypeUpdateRequest request = new ProductTypeUpdateRequest();
        request.setLabel(label);
        return request;
    }

    @Test
    void update_notOwner_throwsAccessDenied() {
        Store otherStore = new Store();
        otherStore.setId(UUID.randomUUID());
        when(currentStoreResolver.getCurrentStore()).thenReturn(otherStore);

        assertThatThrownBy(() -> service.update(STORE_ID, 10L, updateRequest("Perfumes")))
                .isInstanceOf(AccessDeniedException.class);
        verify(productTypeRepository, never()).save(any());
    }

    @Test
    void update_productTypeNotFound_throwsResourceNotFound() {
        when(currentStoreResolver.getCurrentStore()).thenReturn(store);
        when(productTypeRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(STORE_ID, 10L, updateRequest("Perfumes")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_productTypeFromAnotherStore_throwsAccessDenied() {
        Store otherStore = new Store();
        otherStore.setId(UUID.randomUUID());
        when(currentStoreResolver.getCurrentStore()).thenReturn(store);
        when(productTypeRepository.findById(10L)).thenReturn(Optional.of(productType(10L, otherStore)));

        assertThatThrownBy(() -> service.update(STORE_ID, 10L, updateRequest("Perfumes")))
                .isInstanceOf(AccessDeniedException.class);
        verify(productTypeRepository, never()).save(any());
    }

    @Test
    void update_validRequest_renamesLabelOnly() {
        ProductType productType = productType(10L, store);
        when(currentStoreResolver.getCurrentStore()).thenReturn(store);
        when(productTypeRepository.findById(10L)).thenReturn(Optional.of(productType));
        when(productTypeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProductType result = service.update(STORE_ID, 10L, updateRequest("Perfumes"));

        assertThat(result.getLabel()).isEqualTo("Perfumes");
        assertThat(result.getKey()).isEqualTo("perfumes");
        verify(productTypeRepository).save(productType);
    }
}

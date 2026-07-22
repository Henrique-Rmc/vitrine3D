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
import com.store.vitrine3d.rest.dto.AttributeDefinitionCreateRequest;
import com.store.vitrine3d.rest.exception.BusinessRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreAttributeDefinitionServiceTest {

    @Mock private AttributeDefinitionRepository attributeDefinitionRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ProductTypeRepository productTypeRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private CurrentStoreResolver currentStoreResolver;

    private StoreAttributeDefinitionService service;

    private static final UUID STORE_ID = UUID.randomUUID();

    private Store store;

    @BeforeEach
    void setUp() {
        service = new StoreAttributeDefinitionService(
                attributeDefinitionRepository, productRepository, productTypeRepository, storeRepository, currentStoreResolver);

        store = new Store();
        store.setId(STORE_ID);

        lenient().when(storeRepository.findById(STORE_ID)).thenReturn(Optional.of(store));
    }

    private AttributeDefinitionCreateRequest createRequest(String key) {
        AttributeDefinitionCreateRequest request = new AttributeDefinitionCreateRequest();
        request.setKey(key);
        request.setLabel("Cor do interior");
        return request;
    }

    private AttributeDefinitionCreateRequest createRequest(String key, Long productTypeId) {
        AttributeDefinitionCreateRequest request = createRequest(key);
        request.setProductTypeId(productTypeId);
        return request;
    }

    private AttributeDefinition definition(Long id, String key, Store owner) {
        AttributeDefinition definition = new AttributeDefinition();
        definition.setId(id);
        definition.setStore(owner);
        definition.setKey(key);
        definition.setType(AttributeType.NUMBER);
        return definition;
    }

    private AttributeDefinition enumDefinition(Long id, String key, Store owner) {
        AttributeDefinition definition = definition(id, key, owner);
        definition.setType(AttributeType.ENUM);
        definition.setEnumOptions(new ArrayList<>());
        return definition;
    }

    private ProductType productType(Long id, Store owner) {
        ProductType productType = new ProductType();
        productType.setId(id);
        productType.setStore(owner);
        productType.setKey("camisa");
        productType.setLabel("Camisa");
        return productType;
    }

    // -------------------------------------------------------------------------
    // createCustom
    // -------------------------------------------------------------------------

    @Test
    void createCustom_notOwner_throwsAccessDenied() {
        Store otherStore = new Store();
        otherStore.setId(UUID.randomUUID());
        when(currentStoreResolver.getCurrentStore()).thenReturn(otherStore);

        assertThatThrownBy(() -> service.createCustom(STORE_ID, createRequest("corInterior")))
                .isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(attributeDefinitionRepository);
    }

    @Test
    void createCustom_keyCollides_throws() {
        when(currentStoreResolver.getCurrentStore()).thenReturn(store);
        when(attributeDefinitionRepository.findEffectiveForStore(STORE_ID, null))
                .thenReturn(List.of(definition(10L, "marca", store)));

        assertThatThrownBy(() -> service.createCustom(STORE_ID, createRequest("marca")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("marca");
        verify(attributeDefinitionRepository, never()).save(any());
    }

    @Test
    void createCustom_validRequest_savesWithStoreSet() {
        when(currentStoreResolver.getCurrentStore()).thenReturn(store);
        when(attributeDefinitionRepository.findEffectiveForStore(STORE_ID, null)).thenReturn(List.of());
        when(attributeDefinitionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<AttributeDefinition> captor = ArgumentCaptor.forClass(AttributeDefinition.class);

        AttributeDefinition result = service.createCustom(STORE_ID, createRequest("corInterior"));

        verify(attributeDefinitionRepository).save(captor.capture());
        assertThat(captor.getValue().getStore()).isSameAs(store);
        assertThat(captor.getValue().getProductType()).isNull();
        assertThat(result.getKey()).isEqualTo("corInterior");
    }

    @Test
    void createCustom_productTypeFromAnotherStore_throwsAccessDenied() {
        Store otherStore = new Store();
        otherStore.setId(UUID.randomUUID());
        when(currentStoreResolver.getCurrentStore()).thenReturn(store);
        when(productTypeRepository.findById(50L)).thenReturn(Optional.of(productType(50L, otherStore)));

        assertThatThrownBy(() -> service.createCustom(STORE_ID, createRequest("franquia", 50L)))
                .isInstanceOf(AccessDeniedException.class);
        verify(attributeDefinitionRepository, never()).save(any());
    }

    @Test
    void createCustom_scopedToProductType_savesWithProductTypeSet() {
        ProductType camisa = productType(50L, store);
        when(currentStoreResolver.getCurrentStore()).thenReturn(store);
        when(productTypeRepository.findById(50L)).thenReturn(Optional.of(camisa));
        when(attributeDefinitionRepository.findEffectiveForStore(STORE_ID, 50L)).thenReturn(List.of());
        when(attributeDefinitionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<AttributeDefinition> captor = ArgumentCaptor.forClass(AttributeDefinition.class);

        service.createCustom(STORE_ID, createRequest("franquia", 50L));

        verify(attributeDefinitionRepository).save(captor.capture());
        assertThat(captor.getValue().getProductType()).isSameAs(camisa);
    }

    @Test
    void createCustom_scopedToProductType_collidesWithGeneralAttribute_throws() {
        ProductType camisa = productType(50L, store);
        when(currentStoreResolver.getCurrentStore()).thenReturn(store);
        when(productTypeRepository.findById(50L)).thenReturn(Optional.of(camisa));
        // findEffectiveForStore(store, 50L) ja inclui atributos gerais da loja (productType null)
        when(attributeDefinitionRepository.findEffectiveForStore(STORE_ID, 50L))
                .thenReturn(List.of(definition(11L, "tamanho", store)));

        assertThatThrownBy(() -> service.createCustom(STORE_ID, createRequest("tamanho", 50L)))
                .isInstanceOf(BusinessRuleException.class);
        verify(attributeDefinitionRepository, never()).save(any());
    }

    @Test
    void createCustom_generalAttribute_collidesWithProductTypeScoped_throws() {
        AttributeDefinition scoped = definition(12L, "franquia", store);
        scoped.setProductType(productType(50L, store));
        when(currentStoreResolver.getCurrentStore()).thenReturn(store);
        when(attributeDefinitionRepository.findEffectiveForStore(STORE_ID, null)).thenReturn(List.of());
        when(attributeDefinitionRepository.findByStoreId(STORE_ID)).thenReturn(List.of(scoped));

        assertThatThrownBy(() -> service.createCustom(STORE_ID, createRequest("franquia")))
                .isInstanceOf(BusinessRuleException.class);
        verify(attributeDefinitionRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // deleteCustom
    // -------------------------------------------------------------------------

    @Test
    void deleteCustom_attributeFromAnotherStore_throwsAccessDenied() {
        Store otherStore = new Store();
        otherStore.setId(UUID.randomUUID());
        when(currentStoreResolver.getCurrentStore()).thenReturn(store);
        when(attributeDefinitionRepository.findById(10L))
                .thenReturn(Optional.of(definition(10L, "corInterior", otherStore)));

        assertThatThrownBy(() -> service.deleteCustom(STORE_ID, 10L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void deleteCustom_attributeInUse_throwsBusinessRuleException() {
        when(currentStoreResolver.getCurrentStore()).thenReturn(store);
        when(attributeDefinitionRepository.findById(10L))
                .thenReturn(Optional.of(definition(10L, "corInterior", store)));
        when(productRepository.exists(any(Specification.class))).thenReturn(true);

        assertThatThrownBy(() -> service.deleteCustom(STORE_ID, 10L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("corInterior");
        verify(attributeDefinitionRepository, never()).delete(any());
    }

    @Test
    void deleteCustom_notInUse_deletesSuccessfully() {
        AttributeDefinition definition = definition(10L, "corInterior", store);
        when(currentStoreResolver.getCurrentStore()).thenReturn(store);
        when(attributeDefinitionRepository.findById(10L)).thenReturn(Optional.of(definition));
        when(productRepository.exists(any(Specification.class))).thenReturn(false);

        service.deleteCustom(STORE_ID, 10L);

        verify(attributeDefinitionRepository).delete(definition);
    }

    // -------------------------------------------------------------------------
    // listEffective
    // -------------------------------------------------------------------------

    @Test
    void listEffective_delegatesToRepository() {
        when(attributeDefinitionRepository.findEffectiveForStore(STORE_ID, null))
                .thenReturn(List.of(definition(10L, "marca", store), definition(11L, "ano", store)));

        List<AttributeDefinition> result = service.listEffective(STORE_ID, null);

        assertThat(result).extracting(AttributeDefinition::getKey).containsExactly("marca", "ano");
    }

    // -------------------------------------------------------------------------
    // addOption
    // -------------------------------------------------------------------------

    @Test
    void addOption_notEnumType_throws() {
        when(currentStoreResolver.getCurrentStore()).thenReturn(store);
        when(attributeDefinitionRepository.findById(10L)).thenReturn(Optional.of(definition(10L, "marca", store)));

        assertThatThrownBy(() -> service.addOption(STORE_ID, 10L, "Toyota"))
                .isInstanceOf(BusinessRuleException.class);
        verify(attributeDefinitionRepository, never()).save(any());
    }

    @Test
    void addOption_blankValue_throws() {
        when(currentStoreResolver.getCurrentStore()).thenReturn(store);
        when(attributeDefinitionRepository.findById(10L)).thenReturn(Optional.of(enumDefinition(10L, "material", store)));

        assertThatThrownBy(() -> service.addOption(STORE_ID, 10L, "   "))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void addOption_fromAnotherStore_throwsAccessDenied() {
        Store otherStore = new Store();
        otherStore.setId(UUID.randomUUID());
        when(currentStoreResolver.getCurrentStore()).thenReturn(store);
        when(attributeDefinitionRepository.findById(10L))
                .thenReturn(Optional.of(enumDefinition(10L, "corInterior", otherStore)));

        assertThatThrownBy(() -> service.addOption(STORE_ID, 10L, "Preto"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void addOption_addsDirectlyToEnumOptions() {
        AttributeDefinition definition = enumDefinition(10L, "corInterior", store);
        when(currentStoreResolver.getCurrentStore()).thenReturn(store);
        when(attributeDefinitionRepository.findById(10L)).thenReturn(Optional.of(definition));

        service.addOption(STORE_ID, 10L, "Preto");

        assertThat(definition.getEnumOptions()).containsExactly("Preto");
        verify(attributeDefinitionRepository).save(definition);
    }

    @Test
    void addOption_duplicateValue_throws() {
        AttributeDefinition definition = enumDefinition(10L, "corInterior", store);
        definition.getEnumOptions().add("Preto");
        when(currentStoreResolver.getCurrentStore()).thenReturn(store);
        when(attributeDefinitionRepository.findById(10L)).thenReturn(Optional.of(definition));

        assertThatThrownBy(() -> service.addOption(STORE_ID, 10L, "Preto"))
                .isInstanceOf(BusinessRuleException.class);
        verify(attributeDefinitionRepository, never()).save(any());
    }
}

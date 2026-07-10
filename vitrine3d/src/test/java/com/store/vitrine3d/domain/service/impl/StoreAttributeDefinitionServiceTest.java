package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.AttributeDefinition;
import com.store.vitrine3d.domain.model.AttributeType;
import com.store.vitrine3d.domain.model.BusinessType;
import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.repository.AttributeDefinitionRepository;
import com.store.vitrine3d.domain.repository.ProductRepository;
import com.store.vitrine3d.domain.repository.StoreAttributeOptionRepository;
import com.store.vitrine3d.domain.repository.StoreRepository;
import com.store.vitrine3d.domain.service.CurrentStoreResolver;
import com.store.vitrine3d.rest.dto.AttributeDefinitionCreateRequest;
import com.store.vitrine3d.rest.exception.BusinessRuleException;
import com.store.vitrine3d.rest.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoreAttributeDefinitionServiceTest {

    @Mock private AttributeDefinitionRepository attributeDefinitionRepository;
    @Mock private StoreAttributeOptionRepository storeAttributeOptionRepository;
    @Mock private ProductRepository productRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private CurrentStoreResolver currentStoreResolver;

    private StoreAttributeDefinitionService service;

    private static final UUID STORE_ID = UUID.randomUUID();

    private BusinessType businessType;
    private Store store;

    @BeforeEach
    void setUp() {
        EffectiveAttributeDefinitionResolver resolver =
                new EffectiveAttributeDefinitionResolver(storeAttributeOptionRepository);
        service = new StoreAttributeDefinitionService(attributeDefinitionRepository, storeAttributeOptionRepository,
                productRepository, storeRepository, currentStoreResolver, resolver);

        businessType = new BusinessType();
        businessType.setId(1L);
        businessType.setName("Automoveis");

        store = new Store();
        store.setId(STORE_ID);
        store.setBusinessType(businessType);
        store.setHiddenAttributeDefinitionIds(new HashSet<>());

        lenient().when(storeRepository.findById(STORE_ID)).thenReturn(Optional.of(store));
    }

    private AttributeDefinitionCreateRequest createRequest(String key) {
        AttributeDefinitionCreateRequest request = new AttributeDefinitionCreateRequest();
        request.setKey(key);
        request.setLabel("Cor do interior");
        request.setType(AttributeType.TEXT);
        return request;
    }

    private AttributeDefinition globalDefinition(Long id, String key) {
        AttributeDefinition definition = new AttributeDefinition();
        definition.setId(id);
        definition.setBusinessType(businessType);
        definition.setKey(key);
        definition.setType(AttributeType.TEXT);
        return definition;
    }

    private AttributeDefinition customDefinition(Long id, String key, Store owner) {
        AttributeDefinition definition = globalDefinition(id, key);
        definition.setStore(owner);
        return definition;
    }

    private AttributeDefinition globalEnumDefinition(Long id, String key) {
        AttributeDefinition definition = globalDefinition(id, key);
        definition.setType(AttributeType.ENUM);
        definition.setEnumOptions(new ArrayList<>());
        return definition;
    }

    private AttributeDefinition customEnumDefinition(Long id, String key, Store owner) {
        AttributeDefinition definition = customDefinition(id, key, owner);
        definition.setType(AttributeType.ENUM);
        definition.setEnumOptions(new ArrayList<>());
        return definition;
    }

    // -------------------------------------------------------------------------
    // createCustom
    // -------------------------------------------------------------------------

    @Test
    void createCustom_withoutBusinessType_throws() {
        Store storeWithoutType = new Store();
        storeWithoutType.setId(STORE_ID);
        when(storeRepository.findById(STORE_ID)).thenReturn(Optional.of(storeWithoutType));
        when(currentStoreResolver.getCurrentStore()).thenReturn(storeWithoutType);

        assertThatThrownBy(() -> service.createCustom(STORE_ID, createRequest("corInterior")))
                .isInstanceOf(BusinessRuleException.class);
    }

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
    void createCustom_keyCollidesWithGlobal_throws() {
        when(currentStoreResolver.getCurrentStore()).thenReturn(store);
        when(attributeDefinitionRepository.findEffectiveForStore(1L, STORE_ID))
                .thenReturn(List.of(globalDefinition(10L, "marca")));

        assertThatThrownBy(() -> service.createCustom(STORE_ID, createRequest("marca")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("marca");
        verify(attributeDefinitionRepository, never()).save(any());
    }

    @Test
    void createCustom_validRequest_savesWithStoreSet() {
        when(currentStoreResolver.getCurrentStore()).thenReturn(store);
        when(attributeDefinitionRepository.findEffectiveForStore(1L, STORE_ID)).thenReturn(List.of());
        when(attributeDefinitionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<AttributeDefinition> captor = ArgumentCaptor.forClass(AttributeDefinition.class);

        AttributeDefinition result = service.createCustom(STORE_ID, createRequest("corInterior"));

        verify(attributeDefinitionRepository).save(captor.capture());
        assertThat(captor.getValue().getStore()).isSameAs(store);
        assertThat(captor.getValue().getBusinessType()).isSameAs(businessType);
        assertThat(result.getKey()).isEqualTo("corInterior");
    }

    // -------------------------------------------------------------------------
    // deleteCustom
    // -------------------------------------------------------------------------

    @Test
    void deleteCustom_globalAttribute_throwsAccessDenied() {
        when(currentStoreResolver.getCurrentStore()).thenReturn(store);
        when(attributeDefinitionRepository.findById(10L)).thenReturn(Optional.of(globalDefinition(10L, "marca")));

        assertThatThrownBy(() -> service.deleteCustom(STORE_ID, 10L))
                .isInstanceOf(AccessDeniedException.class);
        verify(attributeDefinitionRepository, never()).delete(any());
    }

    @Test
    void deleteCustom_attributeFromAnotherStore_throwsAccessDenied() {
        Store otherStore = new Store();
        otherStore.setId(UUID.randomUUID());
        when(currentStoreResolver.getCurrentStore()).thenReturn(store);
        when(attributeDefinitionRepository.findById(10L))
                .thenReturn(Optional.of(customDefinition(10L, "corInterior", otherStore)));

        assertThatThrownBy(() -> service.deleteCustom(STORE_ID, 10L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void deleteCustom_attributeInUse_throwsBusinessRuleException() {
        when(currentStoreResolver.getCurrentStore()).thenReturn(store);
        when(attributeDefinitionRepository.findById(10L))
                .thenReturn(Optional.of(customDefinition(10L, "corInterior", store)));
        when(productRepository.exists(any(Specification.class))).thenReturn(true);

        assertThatThrownBy(() -> service.deleteCustom(STORE_ID, 10L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("corInterior");
        verify(attributeDefinitionRepository, never()).delete(any());
    }

    @Test
    void deleteCustom_notInUse_deletesSuccessfully() {
        AttributeDefinition definition = customDefinition(10L, "corInterior", store);
        when(currentStoreResolver.getCurrentStore()).thenReturn(store);
        when(attributeDefinitionRepository.findById(10L)).thenReturn(Optional.of(definition));
        when(productRepository.exists(any(Specification.class))).thenReturn(false);

        service.deleteCustom(STORE_ID, 10L);

        verify(attributeDefinitionRepository).delete(definition);
    }

    // -------------------------------------------------------------------------
    // hideGlobal / unhideGlobal
    // -------------------------------------------------------------------------

    @Test
    void hideGlobal_onCustomAttribute_throwsBusinessRuleException() {
        when(currentStoreResolver.getCurrentStore()).thenReturn(store);
        when(attributeDefinitionRepository.findById(10L))
                .thenReturn(Optional.of(customDefinition(10L, "corInterior", store)));

        assertThatThrownBy(() -> service.hideGlobal(STORE_ID, 10L))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void hideGlobal_fromDifferentBusinessType_throwsAccessDenied() {
        BusinessType otherType = new BusinessType();
        otherType.setId(2L);
        AttributeDefinition definition = new AttributeDefinition();
        definition.setId(10L);
        definition.setBusinessType(otherType);
        definition.setKey("area");

        when(currentStoreResolver.getCurrentStore()).thenReturn(store);
        when(attributeDefinitionRepository.findById(10L)).thenReturn(Optional.of(definition));

        assertThatThrownBy(() -> service.hideGlobal(STORE_ID, 10L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void hideGlobal_validGlobalAttribute_addsToHiddenSetAndSavesStore() {
        when(currentStoreResolver.getCurrentStore()).thenReturn(store);
        when(attributeDefinitionRepository.findById(10L)).thenReturn(Optional.of(globalDefinition(10L, "marca")));

        service.hideGlobal(STORE_ID, 10L);

        assertThat(store.getHiddenAttributeDefinitionIds()).containsExactly(10L);
        verify(storeRepository).save(store);
    }

    @Test
    void unhideGlobal_removesFromHiddenSet() {
        store.setHiddenAttributeDefinitionIds(new HashSet<>(Set.of(10L)));
        when(currentStoreResolver.getCurrentStore()).thenReturn(store);
        when(attributeDefinitionRepository.findById(10L)).thenReturn(Optional.of(globalDefinition(10L, "marca")));

        service.unhideGlobal(STORE_ID, 10L);

        assertThat(store.getHiddenAttributeDefinitionIds()).isEmpty();
        verify(storeRepository).save(store);
    }

    // -------------------------------------------------------------------------
    // listEffective
    // -------------------------------------------------------------------------

    @Test
    void listEffective_excludesHiddenGlobals() {
        store.setHiddenAttributeDefinitionIds(new HashSet<>(Set.of(10L)));
        when(attributeDefinitionRepository.findEffectiveForStore(1L, STORE_ID))
                .thenReturn(List.of(globalDefinition(10L, "marca"), globalDefinition(11L, "ano")));

        List<AttributeDefinition> result = service.listEffective(STORE_ID);

        assertThat(result).extracting(AttributeDefinition::getKey).containsExactly("ano");
    }

    @Test
    void listEffective_storeNotFound_throwsResourceNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        when(storeRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.listEffective(unknownId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // addOption
    // -------------------------------------------------------------------------

    @Test
    void addOption_notEnumType_throws() {
        when(currentStoreResolver.getCurrentStore()).thenReturn(store);
        when(attributeDefinitionRepository.findById(10L)).thenReturn(Optional.of(globalDefinition(10L, "marca")));

        assertThatThrownBy(() -> service.addOption(STORE_ID, 10L, "Toyota"))
                .isInstanceOf(BusinessRuleException.class);
        verify(attributeDefinitionRepository, never()).save(any());
    }

    @Test
    void addOption_blankValue_throws() {
        when(currentStoreResolver.getCurrentStore()).thenReturn(store);
        when(attributeDefinitionRepository.findById(10L)).thenReturn(Optional.of(globalEnumDefinition(10L, "material")));

        assertThatThrownBy(() -> service.addOption(STORE_ID, 10L, "   "))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void addOption_globalAttribute_wrongBusinessType_throwsAccessDenied() {
        BusinessType otherType = new BusinessType();
        otherType.setId(2L);
        AttributeDefinition definition = globalEnumDefinition(10L, "tipoImovel");
        definition.setBusinessType(otherType);

        when(currentStoreResolver.getCurrentStore()).thenReturn(store);
        when(attributeDefinitionRepository.findById(10L)).thenReturn(Optional.of(definition));

        assertThatThrownBy(() -> service.addOption(STORE_ID, 10L, "Casa"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void addOption_customAttribute_fromAnotherStore_throwsAccessDenied() {
        Store otherStore = new Store();
        otherStore.setId(UUID.randomUUID());
        when(currentStoreResolver.getCurrentStore()).thenReturn(store);
        when(attributeDefinitionRepository.findById(10L))
                .thenReturn(Optional.of(customEnumDefinition(10L, "corInterior", otherStore)));

        assertThatThrownBy(() -> service.addOption(STORE_ID, 10L, "Preto"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void addOption_globalAttribute_registersPerStoreValue() {
        AttributeDefinition definition = globalEnumDefinition(10L, "material");
        when(currentStoreResolver.getCurrentStore()).thenReturn(store);
        when(attributeDefinitionRepository.findById(10L)).thenReturn(Optional.of(definition));
        when(storeAttributeOptionRepository.existsByStoreIdAndAttributeDefinitionIdAndValue(STORE_ID, 10L, "Ouro"))
                .thenReturn(false);
        when(storeAttributeOptionRepository.countByStoreIdAndAttributeDefinitionId(STORE_ID, 10L)).thenReturn(0L);
        com.store.vitrine3d.domain.model.StoreAttributeOption savedOuro = new com.store.vitrine3d.domain.model.StoreAttributeOption();
        savedOuro.setValue("Ouro");
        when(storeAttributeOptionRepository.findByStoreIdAndAttributeDefinitionIdOrderBySortOrderAsc(STORE_ID, 10L))
                .thenReturn(List.of(savedOuro));

        AttributeDefinition result = service.addOption(STORE_ID, 10L, "Ouro");

        verify(storeAttributeOptionRepository).save(argThat(option ->
                option.getValue().equals("Ouro") && option.getSortOrder() == 0
                        && option.getStore() == store && option.getAttributeDefinition() == definition));
        verify(attributeDefinitionRepository, never()).save(any());
        // Nao deve tocar enumOptions da entidade global compartilhada.
        assertThat(definition.getEnumOptions()).isEmpty();
        assertThat(result.getEnumOptions()).containsExactly("Ouro");
    }

    @Test
    void addOption_globalAttribute_duplicateValue_throws() {
        when(currentStoreResolver.getCurrentStore()).thenReturn(store);
        when(attributeDefinitionRepository.findById(10L)).thenReturn(Optional.of(globalEnumDefinition(10L, "material")));
        when(storeAttributeOptionRepository.existsByStoreIdAndAttributeDefinitionIdAndValue(STORE_ID, 10L, "Ouro"))
                .thenReturn(true);

        assertThatThrownBy(() -> service.addOption(STORE_ID, 10L, "Ouro"))
                .isInstanceOf(BusinessRuleException.class);
        verify(storeAttributeOptionRepository, never()).save(any());
    }

    @Test
    void addOption_customAttribute_addsDirectlyToEnumOptions() {
        AttributeDefinition definition = customEnumDefinition(10L, "corInterior", store);
        when(currentStoreResolver.getCurrentStore()).thenReturn(store);
        when(attributeDefinitionRepository.findById(10L)).thenReturn(Optional.of(definition));

        service.addOption(STORE_ID, 10L, "Preto");

        assertThat(definition.getEnumOptions()).containsExactly("Preto");
        verify(attributeDefinitionRepository).save(definition);
        verifyNoInteractions(storeAttributeOptionRepository);
    }

    @Test
    void addOption_customAttribute_duplicateValue_throws() {
        AttributeDefinition definition = customEnumDefinition(10L, "corInterior", store);
        definition.getEnumOptions().add("Preto");
        when(currentStoreResolver.getCurrentStore()).thenReturn(store);
        when(attributeDefinitionRepository.findById(10L)).thenReturn(Optional.of(definition));

        assertThatThrownBy(() -> service.addOption(STORE_ID, 10L, "Preto"))
                .isInstanceOf(BusinessRuleException.class);
        verify(attributeDefinitionRepository, never()).save(any());
    }
}

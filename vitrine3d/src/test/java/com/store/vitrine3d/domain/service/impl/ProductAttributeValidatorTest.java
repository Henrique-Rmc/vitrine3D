package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.AttributeDefinition;
import com.store.vitrine3d.domain.model.AttributeType;
import com.store.vitrine3d.domain.model.BusinessType;
import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.model.StoreAttributeOption;
import com.store.vitrine3d.domain.repository.AttributeDefinitionRepository;
import com.store.vitrine3d.domain.repository.StoreAttributeOptionRepository;
import com.store.vitrine3d.domain.service.AttributeTypeHandlerRegistry;
import com.store.vitrine3d.rest.exception.BusinessRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductAttributeValidatorTest {

    @Mock private AttributeDefinitionRepository attributeDefinitionRepository;
    @Mock private StoreAttributeOptionRepository storeAttributeOptionRepository;

    private final AttributeTypeHandlerRegistry handlerRegistry = new AttributeTypeHandlerRegistry(List.of(
            new NumberAttributeTypeHandler(), new TextAttributeTypeHandler(), new EnumAttributeTypeHandler()));

    private ProductAttributeValidator validator;

    private static final UUID STORE_ID = UUID.randomUUID();

    private BusinessType businessType;
    private Store store;

    @BeforeEach
    void setUp() {
        EffectiveAttributeDefinitionResolver resolver =
                new EffectiveAttributeDefinitionResolver(storeAttributeOptionRepository);
        validator = new ProductAttributeValidator(attributeDefinitionRepository, handlerRegistry, resolver);

        businessType = new BusinessType();
        businessType.setId(1L);
        businessType.setName("Automoveis");

        store = new Store();
        store.setId(STORE_ID);
        store.setBusinessType(businessType);
    }

    private AttributeDefinition definition(String key, AttributeType type, boolean required) {
        AttributeDefinition definition = new AttributeDefinition();
        definition.setKey(key);
        definition.setType(type);
        definition.setRequired(required);
        return definition;
    }

    private AttributeDefinition definitionWithId(Long id, String key, AttributeType type, boolean required) {
        AttributeDefinition definition = definition(key, type, required);
        definition.setId(id);
        return definition;
    }

    @Test
    void validateForCreate_withoutBusinessType_andEmptyAttributes_returnsEmpty() {
        Store storeWithoutType = new Store();

        Map<String, Object> result = validator.validateForCreate(storeWithoutType, Map.of());

        assertThat(result).isEmpty();
    }

    @Test
    void validateForCreate_withoutBusinessType_andNonEmptyAttributes_throws() {
        Store storeWithoutType = new Store();

        assertThatThrownBy(() -> validator.validateForCreate(storeWithoutType, Map.of("marca", "Toyota")))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void validateForCreate_missingRequiredAttribute_throws() {
        when(attributeDefinitionRepository.findEffectiveForStore(1L, STORE_ID))
                .thenReturn(List.of(definition("marca", AttributeType.TEXT, true)));

        assertThatThrownBy(() -> validator.validateForCreate(store, Map.of()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("marca");
    }

    @Test
    void validateForCreate_missingRequiredAttribute_butHiddenByStore_doesNotThrow() {
        AttributeDefinition marca = definitionWithId(5L, "marca", AttributeType.TEXT, true);
        store.setHiddenAttributeDefinitionIds(Set.of(5L));
        when(attributeDefinitionRepository.findEffectiveForStore(1L, STORE_ID)).thenReturn(List.of(marca));

        Map<String, Object> result = validator.validateForCreate(store, Map.of());

        assertThat(result).isEmpty();
    }

    @Test
    void validateForCreate_unknownAttributeKey_throws() {
        when(attributeDefinitionRepository.findEffectiveForStore(1L, STORE_ID))
                .thenReturn(List.of(definition("marca", AttributeType.TEXT, false)));

        assertThatThrownBy(() -> validator.validateForCreate(store, Map.of("cor", "Azul")))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void validateForCreate_validAttributes_returnsNormalizedMap() {
        when(attributeDefinitionRepository.findEffectiveForStore(1L, STORE_ID))
                .thenReturn(List.of(
                        definition("marca", AttributeType.TEXT, true),
                        definition("ano", AttributeType.NUMBER, false)));

        Map<String, Object> result = validator.validateForCreate(store, Map.of("marca", "Toyota", "ano", 2020));

        assertThat(result).containsEntry("marca", "Toyota");
        assertThat(result.get("ano")).isEqualTo(new java.math.BigDecimal("2020"));
    }

    @Test
    void validateForUpdate_mergesIntoExistingAttributes() {
        when(attributeDefinitionRepository.findEffectiveForStore(1L, STORE_ID))
                .thenReturn(List.of(definition("marca", AttributeType.TEXT, false)));

        Map<String, Object> existing = Map.of("marca", "Honda", "ano", new java.math.BigDecimal("2019"));
        Map<String, Object> result = validator.validateForUpdate(store, existing, Map.of("marca", "Toyota"));

        assertThat(result).containsEntry("marca", "Toyota").containsEntry("ano", new java.math.BigDecimal("2019"));
    }

    @Test
    void validateForUpdate_withNoIncomingAttributes_returnsExistingUnchanged() {
        Map<String, Object> existing = Map.of("marca", "Honda");

        Map<String, Object> result = validator.validateForUpdate(store, existing, null);

        assertThat(result).isSameAs(existing);
    }

    // -------------------------------------------------------------------------
    // ENUM globais — isolamento por loja (valores nao vem do seed, a loja cadastra)
    // -------------------------------------------------------------------------

    @Test
    void validateForCreate_globalEnum_acceptsOnlyValuesRegisteredByThisStore() {
        AttributeDefinition material = definitionWithId(7L, "material", AttributeType.ENUM, false);
        // Simula dado "sujo" direto na entidade global — o resolver deve ignorar isso pra
        // atributo global e usar apenas StoreAttributeOptionRepository.
        material.setEnumOptions(List.of("Prata"));
        when(attributeDefinitionRepository.findEffectiveForStore(1L, STORE_ID)).thenReturn(List.of(material));

        StoreAttributeOption ouro = new StoreAttributeOption();
        ouro.setValue("Ouro");
        when(storeAttributeOptionRepository.findByStoreIdAndAttributeDefinitionIdOrderBySortOrderAsc(STORE_ID, 7L))
                .thenReturn(List.of(ouro));

        Map<String, Object> result = validator.validateForCreate(store, Map.of("material", "Ouro"));
        assertThat(result).containsEntry("material", "Ouro");

        assertThatThrownBy(() -> validator.validateForCreate(store, Map.of("material", "Prata")))
                .isInstanceOf(BusinessRuleException.class);
    }
}

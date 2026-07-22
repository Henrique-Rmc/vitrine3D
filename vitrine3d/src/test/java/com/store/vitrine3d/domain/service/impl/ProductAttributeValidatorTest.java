package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.AttributeDefinition;
import com.store.vitrine3d.domain.model.AttributeType;
import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.repository.AttributeDefinitionRepository;
import com.store.vitrine3d.domain.service.AttributeTypeHandlerRegistry;
import com.store.vitrine3d.rest.exception.BusinessRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductAttributeValidatorTest {

    @Mock private AttributeDefinitionRepository attributeDefinitionRepository;

    private final AttributeTypeHandlerRegistry handlerRegistry = new AttributeTypeHandlerRegistry(List.of(
            new NumberAttributeTypeHandler(), new EnumAttributeTypeHandler()));

    private ProductAttributeValidator validator;

    private static final UUID STORE_ID = UUID.randomUUID();

    private Store store;

    @BeforeEach
    void setUp() {
        validator = new ProductAttributeValidator(attributeDefinitionRepository, handlerRegistry);

        store = new Store();
        store.setId(STORE_ID);
    }

    private AttributeDefinition definition(String key, AttributeType type, boolean required) {
        AttributeDefinition definition = new AttributeDefinition();
        definition.setKey(key);
        definition.setType(type);
        definition.setRequired(required);
        return definition;
    }

    private AttributeDefinition enumDefinition(String key, boolean required, List<String> options) {
        AttributeDefinition definition = definition(key, AttributeType.ENUM, required);
        definition.setEnumOptions(new ArrayList<>(options));
        return definition;
    }

    @Test
    void validateForCreate_noAttributes_returnsEmpty() {
        Map<String, Object> result = validator.validateForCreate(store, null, Map.of());

        assertThat(result).isEmpty();
    }

    @Test
    void validateForCreate_unknownAttributeKey_throws() {
        when(attributeDefinitionRepository.findEffectiveForStore(STORE_ID, null))
                .thenReturn(List.of(definition("marca", AttributeType.NUMBER, false)));

        assertThatThrownBy(() -> validator.validateForCreate(store, null, Map.of("cor", "Azul")))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void validateForCreate_validAttributes_returnsNormalizedMap() {
        when(attributeDefinitionRepository.findEffectiveForStore(STORE_ID, null))
                .thenReturn(List.of(
                        enumDefinition("marca", true, List.of("Toyota")),
                        definition("ano", AttributeType.NUMBER, false)));

        Map<String, Object> result = validator.validateForCreate(store, null, Map.of("marca", "Toyota", "ano", 2020));

        assertThat(result).containsEntry("marca", "Toyota");
        assertThat(result.get("ano")).isEqualTo(new java.math.BigDecimal("2020"));
    }

    @Test
    void validateForUpdate_mergesIntoExistingAttributes() {
        when(attributeDefinitionRepository.findEffectiveForStore(STORE_ID, null))
                .thenReturn(List.of(enumDefinition("marca", false, List.of("Toyota", "Honda"))));

        Map<String, Object> existing = Map.of("marca", "Honda", "ano", new java.math.BigDecimal("2019"));
        Map<String, Object> result = validator.validateForUpdate(store, null, existing, Map.of("marca", "Toyota"));

        assertThat(result).containsEntry("marca", "Toyota").containsEntry("ano", new java.math.BigDecimal("2019"));
    }

    @Test
    void validateForUpdate_withNoIncomingAttributes_returnsExistingUnchanged() {
        Map<String, Object> existing = Map.of("marca", "Honda");

        Map<String, Object> result = validator.validateForUpdate(store, null, existing, null);

        assertThat(result).isSameAs(existing);
    }

    @Test
    void validateForCreate_enumValueNotRegistered_throws() {
        when(attributeDefinitionRepository.findEffectiveForStore(STORE_ID, null))
                .thenReturn(List.of(enumDefinition("material", false, List.of("Ouro"))));

        assertThatThrownBy(() -> validator.validateForCreate(store, null, Map.of("material", "Prata")))
                .isInstanceOf(BusinessRuleException.class);
    }

    // -------------------------------------------------------------------------
    // Atributos escopados a ProductType
    // -------------------------------------------------------------------------

    @Test
    void validateForCreate_productTypeScopedAttribute_acceptedWhenProductTypeMatches() {
        when(attributeDefinitionRepository.findEffectiveForStore(STORE_ID, 50L))
                .thenReturn(List.of(enumDefinition("franquia", false, List.of("Anime"))));

        Map<String, Object> result = validator.validateForCreate(store, 50L, Map.of("franquia", "Anime"));

        assertThat(result).containsEntry("franquia", "Anime");
    }

    @Test
    void validateForCreate_productTypeScopedAttribute_unknownWhenNoProductType() {
        when(attributeDefinitionRepository.findEffectiveForStore(STORE_ID, null))
                .thenReturn(List.of());

        assertThatThrownBy(() -> validator.validateForCreate(store, null, Map.of("franquia", "Anime")))
                .isInstanceOf(BusinessRuleException.class);
    }
}

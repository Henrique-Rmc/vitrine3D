package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.AttributeDefinition;
import com.store.vitrine3d.domain.model.AttributeType;
import com.store.vitrine3d.domain.model.Product;
import com.store.vitrine3d.domain.repository.AttributeDefinitionRepository;
import com.store.vitrine3d.domain.service.AttributeTypeHandlerRegistry;
import com.store.vitrine3d.rest.exception.BusinessRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductAttributeFilterBuilderTest {

    @Mock private AttributeDefinitionRepository attributeDefinitionRepository;

    private final AttributeTypeHandlerRegistry handlerRegistry = new AttributeTypeHandlerRegistry(List.of(
            new NumberAttributeTypeHandler(), new EnumAttributeTypeHandler()));

    private ProductAttributeFilterBuilder filterBuilder;

    private static final UUID STORE_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        filterBuilder = new ProductAttributeFilterBuilder(attributeDefinitionRepository, handlerRegistry);
    }

    private AttributeDefinition definition(String key, AttributeType type, boolean filterable) {
        AttributeDefinition definition = new AttributeDefinition();
        definition.setKey(key);
        definition.setType(type);
        definition.setFilterable(filterable);
        if (type == AttributeType.ENUM) {
            definition.setEnumOptions(List.of("Toyota"));
        }
        return definition;
    }

    @Test
    void build_withNullFilters_returnsEmptyListWithoutTouchingRepositories() {
        List<Specification<Product>> result = filterBuilder.build(STORE_ID, null, null);

        assertThat(result).isEmpty();
    }

    @Test
    void build_withUnknownAttributeKey_throws() {
        when(attributeDefinitionRepository.findEffectiveForStore(STORE_ID, null)).thenReturn(List.of());

        assertThatThrownBy(() -> filterBuilder.build(STORE_ID, null, Map.of("cor", "Azul")))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void build_withNonFilterableAttribute_throws() {
        when(attributeDefinitionRepository.findEffectiveForStore(STORE_ID, null))
                .thenReturn(List.of(definition("marca", AttributeType.ENUM, false)));

        assertThatThrownBy(() -> filterBuilder.build(STORE_ID, null, Map.of("marca", "Toyota")))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void build_withEqualityFilter_returnsOneSpecification() {
        when(attributeDefinitionRepository.findEffectiveForStore(STORE_ID, null))
                .thenReturn(List.of(definition("marca", AttributeType.ENUM, true)));

        List<Specification<Product>> result = filterBuilder.build(STORE_ID, null, Map.of("marca", "Toyota"));

        assertThat(result).hasSize(1);
    }

    @Test
    void build_withMinAndMaxRangeFilter_combinesIntoOneSpecification() {
        when(attributeDefinitionRepository.findEffectiveForStore(STORE_ID, null))
                .thenReturn(List.of(definition("ano", AttributeType.NUMBER, true)));

        List<Specification<Product>> result = filterBuilder.build(STORE_ID, null,
                Map.of("ano_min", "2015", "ano_max", "2023"));

        assertThat(result).hasSize(1);
    }

    @Test
    void build_withProductTypeContext_resolvesScopedAttribute() {
        when(attributeDefinitionRepository.findEffectiveForStore(STORE_ID, 50L))
                .thenReturn(List.of(definition("franquia", AttributeType.ENUM, true)));

        List<Specification<Product>> result = filterBuilder.build(STORE_ID, 50L, Map.of("franquia", "Toyota"));

        assertThat(result).hasSize(1);
    }
}

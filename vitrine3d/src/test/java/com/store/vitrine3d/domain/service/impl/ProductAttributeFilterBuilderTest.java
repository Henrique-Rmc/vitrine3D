package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.AttributeDefinition;
import com.store.vitrine3d.domain.model.AttributeType;
import com.store.vitrine3d.domain.model.BusinessType;
import com.store.vitrine3d.domain.model.Product;
import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.repository.AttributeDefinitionRepository;
import com.store.vitrine3d.domain.repository.StoreRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductAttributeFilterBuilderTest {

    @Mock private StoreRepository storeRepository;
    @Mock private AttributeDefinitionRepository attributeDefinitionRepository;

    private final AttributeTypeHandlerRegistry handlerRegistry = new AttributeTypeHandlerRegistry(List.of(
            new NumberAttributeTypeHandler(), new TextAttributeTypeHandler()));

    private ProductAttributeFilterBuilder filterBuilder;

    private static final UUID STORE_ID = UUID.randomUUID();

    private BusinessType businessType;
    private Store store;

    @BeforeEach
    void setUp() {
        filterBuilder = new ProductAttributeFilterBuilder(storeRepository, attributeDefinitionRepository, handlerRegistry);

        businessType = new BusinessType();
        businessType.setId(1L);
        businessType.setName("Automoveis");

        store = new Store();
        store.setId(STORE_ID);
        store.setBusinessType(businessType);
    }

    private AttributeDefinition definition(String key, AttributeType type, boolean filterable) {
        AttributeDefinition definition = new AttributeDefinition();
        definition.setKey(key);
        definition.setType(type);
        definition.setFilterable(filterable);
        return definition;
    }

    @Test
    void build_withNullFilters_returnsEmptyListWithoutTouchingRepositories() {
        List<Specification<Product>> result = filterBuilder.build(STORE_ID, null);

        assertThat(result).isEmpty();
    }

    @Test
    void build_whenStoreHasNoBusinessType_throws() {
        Store storeWithoutType = new Store();
        storeWithoutType.setId(STORE_ID);
        when(storeRepository.findById(STORE_ID)).thenReturn(Optional.of(storeWithoutType));

        assertThatThrownBy(() -> filterBuilder.build(STORE_ID, Map.of("marca", "Toyota")))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void build_withUnknownAttributeKey_throws() {
        when(storeRepository.findById(STORE_ID)).thenReturn(Optional.of(store));
        when(attributeDefinitionRepository.findEffectiveForStore(1L, STORE_ID)).thenReturn(List.of());

        assertThatThrownBy(() -> filterBuilder.build(STORE_ID, Map.of("cor", "Azul")))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void build_withNonFilterableAttribute_throws() {
        when(storeRepository.findById(STORE_ID)).thenReturn(Optional.of(store));
        when(attributeDefinitionRepository.findEffectiveForStore(1L, STORE_ID))
                .thenReturn(List.of(definition("marca", AttributeType.TEXT, false)));

        assertThatThrownBy(() -> filterBuilder.build(STORE_ID, Map.of("marca", "Toyota")))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void build_withEqualityFilter_returnsOneSpecification() {
        when(storeRepository.findById(STORE_ID)).thenReturn(Optional.of(store));
        when(attributeDefinitionRepository.findEffectiveForStore(1L, STORE_ID))
                .thenReturn(List.of(definition("marca", AttributeType.TEXT, true)));

        List<Specification<Product>> result = filterBuilder.build(STORE_ID, Map.of("marca", "Toyota"));

        assertThat(result).hasSize(1);
    }

    @Test
    void build_withMinAndMaxRangeFilter_combinesIntoOneSpecification() {
        when(storeRepository.findById(STORE_ID)).thenReturn(Optional.of(store));
        when(attributeDefinitionRepository.findEffectiveForStore(1L, STORE_ID))
                .thenReturn(List.of(definition("ano", AttributeType.NUMBER, true)));

        List<Specification<Product>> result = filterBuilder.build(STORE_ID,
                Map.of("ano_min", "2015", "ano_max", "2023"));

        assertThat(result).hasSize(1);
    }
}

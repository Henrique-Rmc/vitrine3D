package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.AttributeDefinition;
import com.store.vitrine3d.domain.model.AttributeType;
import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.model.StoreAttributeOption;
import com.store.vitrine3d.domain.repository.StoreAttributeOptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EffectiveAttributeDefinitionResolverTest {

    @Mock private StoreAttributeOptionRepository storeAttributeOptionRepository;

    private EffectiveAttributeDefinitionResolver resolver;

    private static final UUID STORE_ID = UUID.randomUUID();
    private Store store;

    @BeforeEach
    void setUp() {
        resolver = new EffectiveAttributeDefinitionResolver(storeAttributeOptionRepository);
        store = new Store();
        store.setId(STORE_ID);
    }

    private AttributeDefinition definition(Long id, AttributeType type, Store owner) {
        AttributeDefinition definition = new AttributeDefinition();
        definition.setId(id);
        definition.setKey("material");
        definition.setType(type);
        definition.setEnumOptions(new ArrayList<>());
        definition.setStore(owner);
        return definition;
    }

    @Test
    void resolveEnumOptions_nonEnumType_returnsEmpty() {
        AttributeDefinition text = definition(1L, AttributeType.TEXT, null);

        assertThat(resolver.resolveEnumOptions(store, text)).isEmpty();
    }

    @Test
    void withEffectiveOptions_nonEnumType_returnsSameInstance() {
        AttributeDefinition text = definition(1L, AttributeType.TEXT, null);

        assertThat(resolver.withEffectiveOptions(store, text)).isSameAs(text);
    }

    @Test
    void resolveEnumOptions_customAttribute_readsFromEntityDirectly() {
        AttributeDefinition custom = definition(2L, AttributeType.ENUM, store);
        custom.getEnumOptions().add("Preto");

        assertThat(resolver.resolveEnumOptions(store, custom)).containsExactly("Preto");
    }

    @Test
    void withEffectiveOptions_customAttribute_returnsSameInstance() {
        AttributeDefinition custom = definition(2L, AttributeType.ENUM, store);

        assertThat(resolver.withEffectiveOptions(store, custom)).isSameAs(custom);
    }

    @Test
    void resolveEnumOptions_globalAttribute_readsFromStoreAttributeOptionRepository() {
        AttributeDefinition global = definition(3L, AttributeType.ENUM, null);
        global.getEnumOptions().add("Prata"); // dado "sujo" na entidade compartilhada — deve ser ignorado

        StoreAttributeOption ouro = new StoreAttributeOption();
        ouro.setValue("Ouro");
        when(storeAttributeOptionRepository.findByStoreIdAndAttributeDefinitionIdOrderBySortOrderAsc(STORE_ID, 3L))
                .thenReturn(List.of(ouro));

        assertThat(resolver.resolveEnumOptions(store, global)).containsExactly("Ouro");
    }

    @Test
    void withEffectiveOptions_globalAttribute_returnsDetachedCopyNeverPersisted() {
        AttributeDefinition global = definition(3L, AttributeType.ENUM, null);
        when(storeAttributeOptionRepository.findByStoreIdAndAttributeDefinitionIdOrderBySortOrderAsc(STORE_ID, 3L))
                .thenReturn(List.of());

        AttributeDefinition copy = resolver.withEffectiveOptions(store, global);

        assertThat(copy).isNotSameAs(global);
        assertThat(copy.getId()).isEqualTo(global.getId());
        assertThat(copy.getKey()).isEqualTo(global.getKey());
        assertThat(copy.getEnumOptions()).isEmpty();
    }
}

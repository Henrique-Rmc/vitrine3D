package com.store.vitrine3d.domain.service;

import com.store.vitrine3d.domain.model.AttributeType;
import com.store.vitrine3d.domain.service.impl.BooleanAttributeTypeHandler;
import com.store.vitrine3d.domain.service.impl.NumberAttributeTypeHandler;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AttributeTypeHandlerRegistryTest {

    @Test
    void resolve_returnsHandlerRegisteredForType() {
        NumberAttributeTypeHandler numberHandler = new NumberAttributeTypeHandler();
        BooleanAttributeTypeHandler booleanHandler = new BooleanAttributeTypeHandler();
        AttributeTypeHandlerRegistry registry = new AttributeTypeHandlerRegistry(List.of(numberHandler, booleanHandler));

        assertThat(registry.resolve(AttributeType.NUMBER)).isSameAs(numberHandler);
        assertThat(registry.resolve(AttributeType.BOOLEAN)).isSameAs(booleanHandler);
    }

    @Test
    void resolve_throwsWhenNoHandlerRegisteredForType() {
        AttributeTypeHandlerRegistry registry = new AttributeTypeHandlerRegistry(List.of(new NumberAttributeTypeHandler()));

        assertThatThrownBy(() -> registry.resolve(AttributeType.ENUM))
                .isInstanceOf(IllegalStateException.class);
    }
}

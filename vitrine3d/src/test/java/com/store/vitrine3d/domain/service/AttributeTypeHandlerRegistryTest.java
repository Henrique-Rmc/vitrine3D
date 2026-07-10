package com.store.vitrine3d.domain.service;

import com.store.vitrine3d.domain.model.AttributeType;
import com.store.vitrine3d.domain.service.impl.NumberAttributeTypeHandler;
import com.store.vitrine3d.domain.service.impl.TextAttributeTypeHandler;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AttributeTypeHandlerRegistryTest {

    @Test
    void resolve_returnsHandlerRegisteredForType() {
        NumberAttributeTypeHandler numberHandler = new NumberAttributeTypeHandler();
        TextAttributeTypeHandler textHandler = new TextAttributeTypeHandler();
        AttributeTypeHandlerRegistry registry = new AttributeTypeHandlerRegistry(List.of(numberHandler, textHandler));

        assertThat(registry.resolve(AttributeType.NUMBER)).isSameAs(numberHandler);
        assertThat(registry.resolve(AttributeType.TEXT)).isSameAs(textHandler);
    }

    @Test
    void resolve_throwsWhenNoHandlerRegisteredForType() {
        AttributeTypeHandlerRegistry registry = new AttributeTypeHandlerRegistry(List.of(new NumberAttributeTypeHandler()));

        assertThatThrownBy(() -> registry.resolve(AttributeType.ENUM))
                .isInstanceOf(IllegalStateException.class);
    }
}

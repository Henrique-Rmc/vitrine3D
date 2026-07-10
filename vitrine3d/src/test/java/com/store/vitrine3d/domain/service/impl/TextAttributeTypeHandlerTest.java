package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.AttributeDefinition;
import com.store.vitrine3d.domain.model.AttributeType;
import com.store.vitrine3d.rest.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TextAttributeTypeHandlerTest {

    private final TextAttributeTypeHandler handler = new TextAttributeTypeHandler();

    private AttributeDefinition definition() {
        AttributeDefinition definition = new AttributeDefinition();
        definition.setKey("marca");
        definition.setType(AttributeType.TEXT);
        return definition;
    }

    @Test
    void normalize_acceptsNonBlankText() {
        assertThat(handler.normalize(definition(), "Toyota")).isEqualTo("Toyota");
    }

    @Test
    void normalize_rejectsBlankText() {
        assertThatThrownBy(() -> handler.normalize(definition(), "   "))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void normalize_rejectsNonStringValue() {
        assertThatThrownBy(() -> handler.normalize(definition(), 123))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void equalsFilter_buildsSpecification() {
        assertThat(handler.equalsFilter("marca", "Toyota")).isNotNull();
    }
}

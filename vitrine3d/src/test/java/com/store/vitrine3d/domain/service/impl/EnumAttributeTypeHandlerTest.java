package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.AttributeDefinition;
import com.store.vitrine3d.domain.model.AttributeType;
import com.store.vitrine3d.rest.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnumAttributeTypeHandlerTest {

    private final EnumAttributeTypeHandler handler = new EnumAttributeTypeHandler();

    private AttributeDefinition definition() {
        AttributeDefinition definition = new AttributeDefinition();
        definition.setKey("tamanho");
        definition.setType(AttributeType.ENUM);
        definition.setEnumOptions(List.of("PP", "P", "M", "G", "GG"));
        return definition;
    }

    @Test
    void normalize_acceptsValueWithinOptions() {
        assertThat(handler.normalize(definition(), "M")).isEqualTo("M");
    }

    @Test
    void normalize_rejectsValueOutsideOptions() {
        assertThatThrownBy(() -> handler.normalize(definition(), "XG"))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void equalsFilter_buildsSpecification() {
        assertThat(handler.equalsFilter("tamanho", "M")).isNotNull();
    }
}

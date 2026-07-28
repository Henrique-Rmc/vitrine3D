package com.store.vitrine3d.rest.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * getNullValue() é o que permite diferenciar "campo: null" (chamado pelo Jackson) de "campo
 * ausente" (deserializer nunca é chamado, o field fica no default declarado no DTO).
 */
public class PatchFieldStringDeserializer extends JsonDeserializer<PatchField<String>> {

    @Override
    public PatchField<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return PatchField.of(p.getValueAsString());
    }

    @Override
    public PatchField<String> getNullValue(DeserializationContext ctxt) {
        return PatchField.of(null);
    }
}

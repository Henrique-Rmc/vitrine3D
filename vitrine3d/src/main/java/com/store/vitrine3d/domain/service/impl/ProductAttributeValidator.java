package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.AttributeDefinition;
import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.repository.AttributeDefinitionRepository;
import com.store.vitrine3d.domain.service.AttributeTypeHandler;
import com.store.vitrine3d.domain.service.AttributeTypeHandlerRegistry;
import com.store.vitrine3d.rest.exception.BusinessRuleException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Valida os atributos dinamicos de um Product (Product.attributes) contra o schema
 * (AttributeDefinition) da propria loja, opcionalmente escopado a um ProductType.
 */
@Component
public class ProductAttributeValidator {

    private final AttributeDefinitionRepository attributeDefinitionRepository;
    private final AttributeTypeHandlerRegistry handlerRegistry;

    public ProductAttributeValidator(AttributeDefinitionRepository attributeDefinitionRepository,
                                      AttributeTypeHandlerRegistry handlerRegistry) {
        this.attributeDefinitionRepository = attributeDefinitionRepository;
        this.handlerRegistry = handlerRegistry;
    }

    /** Usado na criacao do produto: normaliza os atributos enviados (nenhum e obrigatorio). */
    public Map<String, Object> validateForCreate(Store store, Long productTypeId, Map<String, Object> rawAttributes) {
        Map<String, Object> raw = rawAttributes != null ? rawAttributes : Map.of();
        Map<String, AttributeDefinition> definitionsByKey = resolveDefinitions(store, productTypeId, raw);

        Map<String, Object> normalized = new HashMap<>();
        for (AttributeDefinition definition : definitionsByKey.values()) {
            Object value = raw.get(definition.getKey());
            if (value == null) continue;
            normalized.put(definition.getKey(), normalize(definition, value));
        }
        return normalized;
    }

    /** Usado na atualizacao: so valida/normaliza as chaves enviadas e as sobrepoe nas existentes. */
    public Map<String, Object> validateForUpdate(Store store, Long productTypeId, Map<String, Object> existingAttributes,
                                                  Map<String, Object> incomingAttributes) {
        if (incomingAttributes == null || incomingAttributes.isEmpty()) {
            return existingAttributes;
        }
        Map<String, AttributeDefinition> definitionsByKey = resolveDefinitions(store, productTypeId, incomingAttributes);

        Map<String, Object> merged = new HashMap<>(existingAttributes != null ? existingAttributes : Map.of());
        for (Map.Entry<String, Object> entry : incomingAttributes.entrySet()) {
            AttributeDefinition definition = definitionsByKey.get(entry.getKey());
            merged.put(entry.getKey(), normalize(definition, entry.getValue()));
        }
        return merged;
    }

    private Map<String, AttributeDefinition> resolveDefinitions(Store store, Long productTypeId,
                                                                  Map<String, Object> rawAttributes) {
        Map<String, AttributeDefinition> definitionsByKey = attributeDefinitionRepository
                .findEffectiveForStore(store.getId(), productTypeId).stream()
                .collect(Collectors.toMap(AttributeDefinition::getKey, Function.identity()));

        for (String key : rawAttributes.keySet()) {
            if (!definitionsByKey.containsKey(key)) {
                throw new BusinessRuleException("UNKNOWN_ATTRIBUTE",
                        "Attribute '" + key + "' is not defined for this store.");
            }
        }
        return definitionsByKey;
    }

    private Object normalize(AttributeDefinition definition, Object value) {
        AttributeTypeHandler handler = handlerRegistry.resolve(definition.getType());
        return handler.normalize(definition, value);
    }
}

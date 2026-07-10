package com.store.vitrine3d.domain.service;

import com.store.vitrine3d.domain.model.AttributeType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class AttributeTypeHandlerRegistry {

    private final Map<AttributeType, AttributeTypeHandler> handlersByType;

    public AttributeTypeHandlerRegistry(List<AttributeTypeHandler> handlers) {
        this.handlersByType = new EnumMap<>(AttributeType.class);
        handlers.forEach(handler -> handlersByType.put(handler.type(), handler));
    }

    public AttributeTypeHandler resolve(AttributeType type) {
        AttributeTypeHandler handler = handlersByType.get(type);
        if (handler == null) {
            throw new IllegalStateException("No AttributeTypeHandler registered for type " + type);
        }
        return handler;
    }
}

package com.store.vitrine3d.rest.dto;

import java.util.function.Consumer;

/**
 * Distingue "campo ausente no JSON" de "campo enviado como null" — algo que String/Optional<String>
 * sozinhos não conseguem representar (Optional.of(null) nem compila, e o deserializer padrão do
 * Jackson pro jdk8 module colapsa null explícito e ausência no mesmo Optional.empty()).
 * Ausente = valor do field na declaração (PatchField.absent()), nunca tocado pelo Jackson.
 * Presente = Jackson chamou o deserializer (seja com valor real, seja com token null).
 */
public final class PatchField<T> {

    private static final PatchField<?> ABSENT = new PatchField<>(false, null);

    private final boolean present;
    private final T value;

    private PatchField(boolean present, T value) {
        this.present = present;
        this.value = value;
    }

    @SuppressWarnings("unchecked")
    public static <T> PatchField<T> absent() {
        return (PatchField<T>) ABSENT;
    }

    public static <T> PatchField<T> of(T value) {
        return new PatchField<>(true, value);
    }

    public boolean isPresent() {
        return present;
    }

    public T getValue() {
        return value;
    }

    public void ifPresent(Consumer<T> consumer) {
        if (present) consumer.accept(value);
    }
}

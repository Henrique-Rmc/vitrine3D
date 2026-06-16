package com.store.vitrine3d.rest.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    public static <S, T> PageResponse<T> from(Page<S> pageData, Function<S, T> mapper) {
        return new PageResponse<>(
                pageData.getContent().stream().map(mapper).toList(),
                pageData.getNumber(),
                pageData.getSize(),
                pageData.getTotalElements(),
                pageData.getTotalPages(),
                pageData.isLast()
        );
    }
}

package com.store.vitrine3d.domain.service;

import com.store.vitrine3d.domain.model.StoreMetadata;

import java.util.List;
import java.util.UUID;

public interface StoreMetadataService<T extends StoreMetadata> {
    List<T> findPublic();
    List<T> findByStore(UUID storeId);
    T findById(Long id);
    T create(String name);
    T update(Long id, String name);
    void delete(Long id);
}

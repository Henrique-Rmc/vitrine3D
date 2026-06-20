package com.store.vitrine3d.domain.repository;

import com.store.vitrine3d.domain.model.StoreMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.UUID;

@NoRepositoryBean
public interface StoreMetadataRepository<T extends StoreMetadata> extends JpaRepository<T, Long> {
    List<T> findByIsGlobalTrue();
    List<T> findByIsGlobalTrueOrStoreId(UUID storeId);
}

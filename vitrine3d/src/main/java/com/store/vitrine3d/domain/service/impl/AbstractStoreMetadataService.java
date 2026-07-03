package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.model.StoreMetadata;
import com.store.vitrine3d.domain.repository.StoreMetadataRepository;
import com.store.vitrine3d.domain.service.CurrentStoreResolver;
import com.store.vitrine3d.domain.service.StoreMetadataService;
import com.store.vitrine3d.rest.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Transactional
public abstract class AbstractStoreMetadataService<T extends StoreMetadata>
        implements StoreMetadataService<T> {

    private final StoreMetadataRepository<T> repository;
    private final CurrentStoreResolver currentStoreResolver;

    protected AbstractStoreMetadataService(StoreMetadataRepository<T> repository,
                                           CurrentStoreResolver currentStoreResolver) {
        this.repository = repository;
        this.currentStoreResolver = currentStoreResolver;
    }

    // --- Template Method: subclasses define the entity type ---

    protected abstract T newInstance();

    protected abstract String resourceName();

    // --- Shared algorithm ---

    @Override
    @Transactional(readOnly = true)
    public List<T> findPublic() {
        return repository.findByIsGlobalTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<T> findByStore(UUID storeId) {
        return repository.findByIsGlobalTrueOrStoreId(storeId);
    }

    @Override
    @Transactional(readOnly = true)
    public T findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(resourceName(), id));
    }

    @Override
    public T create(String name) {
        Store store = currentStoreResolver.getCurrentStore();
        T entity = newInstance();
        entity.setName(name);
        entity.setIsGlobal(false);
        entity.setStore(store);
        return repository.save(entity);
    }

    @Override
    public T update(Long id, String name) {
        T entity = findById(id);
        assertOwnership(entity);
        entity.setName(name);
        return repository.save(entity);
    }

    @Override
    public void delete(Long id) {
        T entity = findById(id);
        assertOwnership(entity);
        repository.deleteById(id);
    }

    // --- Shared helpers ---

    private void assertOwnership(T entity) {
        if (Boolean.TRUE.equals(entity.getIsGlobal())) {
            throw new AccessDeniedException("Global " + resourceName().toLowerCase() + "s cannot be modified.");
        }
        Store current = currentStoreResolver.getCurrentStore();
        if (entity.getStore() == null || !entity.getStore().getId().equals(current.getId())) {
            throw new AccessDeniedException(
                    "You do not have permission to modify this " + resourceName().toLowerCase() + ".");
        }
    }
}

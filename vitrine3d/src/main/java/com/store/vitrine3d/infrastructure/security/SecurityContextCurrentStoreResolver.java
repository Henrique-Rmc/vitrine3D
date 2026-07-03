package com.store.vitrine3d.infrastructure.security;

import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.repository.StoreRepository;
import com.store.vitrine3d.domain.service.CurrentStoreResolver;
import com.store.vitrine3d.rest.exception.ResourceNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityContextCurrentStoreResolver implements CurrentStoreResolver {

    private final StoreRepository storeRepository;

    public SecurityContextCurrentStoreResolver(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    @Override
    public Store getCurrentStore() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return storeRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found after authentication"));
    }
}

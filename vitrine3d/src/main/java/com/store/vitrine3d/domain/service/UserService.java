package com.store.vitrine3d.domain.service;

import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.rest.dto.StoreRegisterRequest;

import java.util.Optional;

public interface UserService {
    Store register(StoreRegisterRequest request);
    Optional<Store> findById(Long id);
    Optional<Store> findByEmail(String email);
}

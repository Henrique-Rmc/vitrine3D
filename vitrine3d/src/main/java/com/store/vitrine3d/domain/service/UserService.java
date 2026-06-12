package com.store.vitrine3d.domain.service;

import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.rest.dto.StoreRegisterRequest;
import com.store.vitrine3d.rest.dto.StoreUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface UserService {
    Store register(StoreRegisterRequest request);
    Store update(Long id, StoreUpdateRequest request);
    Store uploadLogo(Long id, MultipartFile logo);
    Optional<Store> findById(Long id);
    Optional<Store> findByEmail(String email);
    Optional<Store> findBySlug(String slug);
}

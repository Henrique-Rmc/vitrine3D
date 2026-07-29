package com.store.vitrine3d.domain.service;

import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.rest.dto.StoreRegisterRequest;
import com.store.vitrine3d.rest.dto.StoreUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    Store register(StoreRegisterRequest request);
    Store registerAffiliate(StoreRegisterRequest request);
    Store update(UUID id, StoreUpdateRequest request);
    Store uploadLogo(UUID id, MultipartFile logo);
    Store uploadCoverImage(UUID id, MultipartFile coverImage);
    Store deleteCoverImage(UUID id);
    Store uploadPromoImages(UUID id, List<MultipartFile> promoImages);
    Optional<Store> findById(UUID id);
    Optional<Store> findByEmail(String email);
    Optional<Store> findBySlug(String slug);
    void verifyEmail(String token);
    void resendVerification(String email);
}

package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.repository.CityRepository;
import com.store.vitrine3d.domain.repository.StateRepository;
import com.store.vitrine3d.domain.repository.StoreRepository;
import com.store.vitrine3d.domain.service.UserService;
import com.store.vitrine3d.infrastructure.storage.StorageService;
import com.store.vitrine3d.rest.dto.StoreRegisterRequest;
import com.store.vitrine3d.rest.dto.StoreUpdateRequest;
import com.store.vitrine3d.rest.exception.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.Normalizer;
import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final StoreRepository storeRepository;
    private final StateRepository stateRepository;
    private final CityRepository cityRepository;
    private final PasswordEncoder passwordEncoder;
    private final StorageService storageService;

    public UserServiceImpl(StoreRepository storeRepository,
                           StateRepository stateRepository,
                           CityRepository cityRepository,
                           PasswordEncoder passwordEncoder,
                           StorageService storageService) {
        this.storeRepository = storeRepository;
        this.stateRepository = stateRepository;
        this.cityRepository = cityRepository;
        this.passwordEncoder = passwordEncoder;
        this.storageService = storageService;
    }

    @Override
    public Store register(StoreRegisterRequest request) {
        if (storeRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("E-mail já cadastrado: " + request.getEmail());
        }

        Store store = new Store();
        store.setEmail(request.getEmail());
        store.setPassword(passwordEncoder.encode(request.getPassword()));
        store.setUserName(request.getUserName());
        store.setStoreName(request.getStoreName());
        store.setSlug(generateUniqueSlug(request.getStoreName()));
        store.setWhatsappNumber(request.getWhatsappNumber());
        store.setStoreDescription(request.getStoreDescription());

        if (request.getStateId() != null) {
            store.setState(stateRepository.findById(request.getStateId())
                    .orElseThrow(() -> new ResourceNotFoundException("Estado", request.getStateId())));
        }
        if (request.getCityId() != null) {
            store.setCity(cityRepository.findById(request.getCityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cidade", request.getCityId())));
        }

        return storeRepository.save(store);
    }

    @Override
    public Store update(Long id, StoreUpdateRequest request) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loja", id));

        if (request.getUserName() != null) store.setUserName(request.getUserName());
        if (request.getWhatsappNumber() != null) store.setWhatsappNumber(request.getWhatsappNumber());
        if (request.getStoreDescription() != null) store.setStoreDescription(request.getStoreDescription());

        if (request.getStoreName() != null && !request.getStoreName().equals(store.getStoreName())) {
            store.setStoreName(request.getStoreName());
            store.setSlug(generateUniqueSlug(request.getStoreName()));
        }

        if (request.getStateId() != null) {
            store.setState(stateRepository.findById(request.getStateId())
                    .orElseThrow(() -> new ResourceNotFoundException("Estado", request.getStateId())));
        }
        if (request.getCityId() != null) {
            store.setCity(cityRepository.findById(request.getCityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cidade", request.getCityId())));
        }

        return storeRepository.save(store);
    }

    @Override
    public Store uploadLogo(Long id, MultipartFile logo) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loja", id));
        store.setLogoUrl(storageService.uploadFile(logo));
        return storeRepository.save(store);
    }

    @Override
    public Optional<Store> findById(Long id) {
        return storeRepository.findById(id);
    }

    @Override
    public Optional<Store> findByEmail(String email) {
        return storeRepository.findByEmail(email);
    }

    @Override
    public Optional<Store> findBySlug(String slug) {
        return storeRepository.findBySlug(slug);
    }

    private String generateUniqueSlug(String storeName) {
        String base = Normalizer.normalize(storeName, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");

        String slug = base;
        int suffix = 2;
        while (storeRepository.existsBySlug(slug)) {
            slug = base + "-" + suffix++;
        }
        return slug;
    }
}

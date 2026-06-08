package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.repository.StoreRepository;
import com.store.vitrine3d.domain.service.UserService;
import com.store.vitrine3d.rest.dto.StoreRegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final StoreRepository storeRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(StoreRepository storeRepository, PasswordEncoder passwordEncoder) {
        this.storeRepository = storeRepository;
        this.passwordEncoder = passwordEncoder;
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
        store.setWhatsappNumber(request.getWhatsappNumber());
        store.setStoreDescription(request.getStoreDescription());

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
}

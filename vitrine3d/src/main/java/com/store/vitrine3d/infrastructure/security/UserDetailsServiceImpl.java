package com.store.vitrine3d.infrastructure.security;

import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.repository.StoreRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final StoreRepository storeRepository;

    public UserDetailsServiceImpl(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Store store = storeRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

        return new User(
                store.getEmail(),
                store.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_STORE_OWNER"))
        );
    }
}

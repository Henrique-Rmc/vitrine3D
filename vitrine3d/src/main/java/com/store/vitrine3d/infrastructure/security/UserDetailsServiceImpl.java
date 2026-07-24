package com.store.vitrine3d.infrastructure.security;

import com.store.vitrine3d.domain.model.AdminUser;
import com.store.vitrine3d.domain.model.Store;
import com.store.vitrine3d.domain.repository.AdminUserRepository;
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

    private final AdminUserRepository adminUserRepository;
    private final StoreRepository storeRepository;

    public UserDetailsServiceImpl(AdminUserRepository adminUserRepository,
                                  StoreRepository storeRepository) {
        this.adminUserRepository = adminUserRepository;
        this.storeRepository = storeRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AdminUser admin = adminUserRepository.findByEmail(email).orElse(null);
        if (admin != null) {
            return new User(admin.getEmail(), admin.getPassword(),
                    admin.isActive(), true, true, true,
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        }

        Store store = storeRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return new User(store.getEmail(), store.getPassword(),
                Boolean.TRUE.equals(store.getIsActive()), true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_STORE_OWNER")));
    }
}

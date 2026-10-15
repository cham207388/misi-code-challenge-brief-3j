package com.wsllclabs.memorialops.identity.service;

import com.wsllclabs.memorialops.identity.entity.AppUser;
import com.wsllclabs.memorialops.identity.entity.RoleEntity;
import com.wsllclabs.memorialops.identity.repository.AppUserRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemorialUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    public MemorialUserDetailsService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        AppUser appUser = appUserRepository
                .findByUsernameIgnoreCaseAndDeletedAtIsNull(username)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
        if (!appUser.isActive()) {
            throw new DisabledException("Invalid credentials");
        }
        return User.withUsername(appUser.getUsername())
                .password(appUser.getPasswordHash())
                .authorities(appUser.getRoles().stream()
                        .map(RoleEntity::getCode)
                        .map(code -> new SimpleGrantedAuthority("ROLE_" + code))
                        .toList())
                .disabled(!appUser.isActive())
                .build();
    }
}

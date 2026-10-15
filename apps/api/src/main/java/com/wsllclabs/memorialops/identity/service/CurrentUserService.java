package com.wsllclabs.memorialops.identity.service;

import com.wsllclabs.memorialops.identity.dto.MeResponse;
import com.wsllclabs.memorialops.identity.entity.AppUser;
import com.wsllclabs.memorialops.identity.entity.RoleEntity;
import com.wsllclabs.memorialops.identity.repository.AppUserRepository;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CurrentUserService {

    private final AppUserRepository appUserRepository;

    public CurrentUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Transactional(readOnly = true)
    public MeResponse current() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("Not authenticated");
        }
        return toResponse(requireUser(authentication.getName()));
    }

    @Transactional(readOnly = true)
    public MeResponse forUsername(String username) {
        return toResponse(requireUser(username));
    }

    @Transactional(readOnly = true)
    public AppUser requireCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UsernameNotFoundException("Not authenticated");
        }
        return requireUser(authentication.getName());
    }

    private AppUser requireUser(String username) {
        return appUserRepository
                .findByUsernameIgnoreCaseAndDeletedAtIsNull(username)
                .orElseThrow(() -> new UsernameNotFoundException("Not authenticated"));
    }

    private static MeResponse toResponse(AppUser appUser) {
        List<String> roles =
                appUser.getRoles().stream().map(RoleEntity::getCode).sorted().toList();
        return new MeResponse(
                appUser.getId(),
                appUser.getUsername(),
                appUser.getDisplayName(),
                roles,
                List.copyOf(CapabilityCatalog.capabilitiesFor(roles)));
    }
}

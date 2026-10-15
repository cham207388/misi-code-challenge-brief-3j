package com.wsllclabs.memorialops.identity.repository;

import com.wsllclabs.memorialops.identity.entity.AppUser;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    Optional<AppUser> findByUsernameIgnoreCaseAndDeletedAtIsNull(String username);
}

package com.wsllclabs.memorialops.application;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationCaseRepository extends JpaRepository<ApplicationCase, UUID> {

    Optional<ApplicationCase> findByIdAndDeletedAtIsNull(UUID id);

    Page<ApplicationCase> findByDeletedAtIsNull(Pageable pageable);

    Page<ApplicationCase> findByDeletedAtIsNullAndOwnerId(UUID ownerId, Pageable pageable);

    Page<ApplicationCase> findByDeletedAtIsNullAndStatus(ApplicationStatus status, Pageable pageable);

    Page<ApplicationCase> findByDeletedAtIsNullAndOwnerIdAndStatus(
            UUID ownerId, ApplicationStatus status, Pageable pageable);

    Page<ApplicationCase> findByDeletedAtIsNullAndStatusIn(Collection<ApplicationStatus> statuses, Pageable pageable);

    Page<ApplicationCase> findByDeletedAtIsNullAndIdIn(Collection<UUID> ids, Pageable pageable);

    long countByDeletedAtIsNull();
}

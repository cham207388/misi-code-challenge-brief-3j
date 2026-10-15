package com.wsllclabs.memorialops.application;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRecordRepository extends JpaRepository<EventRecord, UUID> {

    Optional<EventRecord> findByApplicationId(UUID applicationId);
}

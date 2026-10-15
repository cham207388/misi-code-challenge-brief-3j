package com.wsllclabs.memorialops.application;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusHistoryRepository extends JpaRepository<StatusHistory, UUID> {

    List<StatusHistory> findByApplicationIdOrderByOccurredAtAsc(UUID applicationId);
}

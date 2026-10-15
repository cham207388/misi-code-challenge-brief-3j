package com.wsllclabs.memorialops.application;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventAssignmentRepository extends JpaRepository<EventAssignment, UUID> {

    boolean existsByApplicationIdAndAttendantId(UUID applicationId, UUID attendantId);

    List<EventAssignment> findByAttendantId(UUID attendantId);
}

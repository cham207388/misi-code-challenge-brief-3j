package com.wsllclabs.memorialops.audit;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEventEntity, UUID> {

    List<AuditEventEntity> findByEntityTypeAndEntityIdOrderByOccurredAtAsc(String entityType, UUID entityId);
}

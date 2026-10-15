package com.wsllclabs.memorialops.scheduling;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CemeteryRepository extends JpaRepository<Cemetery, UUID> {}

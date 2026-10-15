package com.wsllclabs.memorialops.scheduling;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenueRepository extends JpaRepository<Venue, UUID> {

    List<Venue> findByCemeteryId(UUID cemeteryId);
}

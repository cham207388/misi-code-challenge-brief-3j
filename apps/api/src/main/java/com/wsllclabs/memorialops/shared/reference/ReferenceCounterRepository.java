package com.wsllclabs.memorialops.shared.reference;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReferenceCounterRepository extends JpaRepository<ReferenceCounter, ReferenceCounter.Key> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ReferenceCounter c where c.kind = :kind and c.year = :year")
    Optional<ReferenceCounter> lockByKindAndYear(@Param("kind") String kind, @Param("year") int year);
}

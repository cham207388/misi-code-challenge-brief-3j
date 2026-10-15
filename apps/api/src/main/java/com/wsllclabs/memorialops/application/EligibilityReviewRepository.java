package com.wsllclabs.memorialops.application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EligibilityReviewRepository extends JpaRepository<EligibilityReview, UUID> {

    Optional<EligibilityReview> findFirstByApplicationIdOrderByCreatedAtDesc(UUID applicationId);

    List<EligibilityReview> findByApplicationId(UUID applicationId);
}

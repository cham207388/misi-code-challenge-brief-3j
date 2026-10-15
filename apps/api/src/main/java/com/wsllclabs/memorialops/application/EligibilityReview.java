package com.wsllclabs.memorialops.application;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "eligibility_review")
public class EligibilityReview {

    @Id
    private UUID id;

    @Column(name = "application_id", nullable = false)
    private UUID applicationId;

    @Column(name = "reviewer_id", nullable = false)
    private UUID reviewerId;

    @Column(nullable = false)
    private String outcome;

    private String notes;

    @Version
    private long version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected EligibilityReview() {}

    public static EligibilityReview record(
            UUID applicationId, UUID reviewerId, String outcome, String notes, Instant now) {
        EligibilityReview review = new EligibilityReview();
        review.id = UUID.randomUUID();
        review.applicationId = applicationId;
        review.reviewerId = reviewerId;
        review.outcome = outcome;
        review.notes = notes;
        review.createdAt = now;
        review.updatedAt = now;
        return review;
    }

    public UUID getReviewerId() {
        return reviewerId;
    }

    public String getOutcome() {
        return outcome;
    }
}

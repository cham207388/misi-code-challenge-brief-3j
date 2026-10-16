package com.wsllclabs.memorialops.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wsllclabs.memorialops.identity.entity.AppUser;
import com.wsllclabs.memorialops.identity.repository.AppUserRepository;
import com.wsllclabs.memorialops.shared.reference.ReferenceNumberService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.DefaultApplicationArguments;

class DemoApplicationSeederTest {

    @Test
    void seedsStatusesThenSkips() {
        ApplicationCaseRepository applications = Mockito.mock(ApplicationCaseRepository.class);
        ApplicantRepository applicants = Mockito.mock(ApplicantRepository.class);
        EventRecordRepository events = Mockito.mock(EventRecordRepository.class);
        StatusHistoryRepository history = Mockito.mock(StatusHistoryRepository.class);
        EligibilityReviewRepository reviews = Mockito.mock(EligibilityReviewRepository.class);
        EventAssignmentRepository assignments = Mockito.mock(EventAssignmentRepository.class);
        AppUserRepository users = Mockito.mock(AppUserRepository.class);
        ReferenceNumberService references = Mockito.mock(ReferenceNumberService.class);
        when(applications.countByDeletedAtIsNull()).thenReturn(0L);
        when(users.findById(any())).thenReturn(Optional.of(Mockito.mock(AppUser.class)));
        AtomicInteger sequence = new AtomicInteger();
        when(references.nextApplicationReference())
                .thenAnswer(invocation -> "APP-2026-%05d".formatted(sequence.incrementAndGet()));
        when(applicants.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(applications.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(events.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(history.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(reviews.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(assignments.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        DemoApplicationSeeder seeder = new DemoApplicationSeeder(
                applications,
                applicants,
                events,
                history,
                reviews,
                assignments,
                users,
                references,
                Clock.fixed(Instant.parse("2026-10-15T12:00:00Z"), ZoneOffset.UTC));
        seeder.run(new DefaultApplicationArguments());
        verify(applications, times(5)).save(any());
        verify(references, times(5)).nextApplicationReference();
        ArgumentCaptor<EligibilityReview> reviewCaptor = ArgumentCaptor.forClass(EligibilityReview.class);
        verify(reviews, times(2)).save(reviewCaptor.capture());
        assertThat(reviewCaptor.getAllValues())
                .extracting(EligibilityReview::getOutcome)
                .containsOnly("ELIGIBLE");
        verify(assignments, times(2)).save(any());
        when(applications.countByDeletedAtIsNull()).thenReturn(5L);
        seeder.run(new DefaultApplicationArguments());
        verify(references, times(5)).nextApplicationReference();
        verify(users, never()).deleteById(any(UUID.class));
    }
}

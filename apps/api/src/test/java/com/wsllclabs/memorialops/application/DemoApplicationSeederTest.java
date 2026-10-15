package com.wsllclabs.memorialops.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.DefaultApplicationArguments;

class DemoApplicationSeederTest {

    @Test
    void seedsOnceThenSkips() {
        ApplicationCaseRepository applications = Mockito.mock(ApplicationCaseRepository.class);
        ApplicantRepository applicants = Mockito.mock(ApplicantRepository.class);
        EventRecordRepository events = Mockito.mock(EventRecordRepository.class);
        StatusHistoryRepository history = Mockito.mock(StatusHistoryRepository.class);
        AppUserRepository users = Mockito.mock(AppUserRepository.class);
        ReferenceNumberService references = Mockito.mock(ReferenceNumberService.class);
        when(applications.countByDeletedAtIsNull()).thenReturn(0L);
        when(users.findById(any())).thenReturn(Optional.of(Mockito.mock(AppUser.class)));
        when(references.nextApplicationReference()).thenReturn("APP-2026-00001");
        when(applicants.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(applications.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        DemoApplicationSeeder seeder = new DemoApplicationSeeder(
                applications,
                applicants,
                events,
                history,
                users,
                references,
                Clock.fixed(Instant.parse("2026-10-15T12:00:00Z"), ZoneOffset.UTC));
        seeder.run(new DefaultApplicationArguments());
        verify(applications).save(any());
        when(applications.countByDeletedAtIsNull()).thenReturn(1L);
        seeder.run(new DefaultApplicationArguments());
        verify(references, Mockito.times(1)).nextApplicationReference();
        verify(users, never()).deleteById(any(UUID.class));
    }
}

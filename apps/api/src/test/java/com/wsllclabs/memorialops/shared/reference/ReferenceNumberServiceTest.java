package com.wsllclabs.memorialops.shared.reference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class ReferenceNumberServiceTest {

    @Mock
    private ReferenceCounterRepository repository;

    @Test
    void incrementsExistingCounter() {
        ReferenceCounter counter = new ReferenceCounter("APP", 2026);
        when(repository.lockByKindAndYear("APP", 2026)).thenReturn(Optional.of(counter));
        ReferenceNumberService service = new ReferenceNumberService(
                repository, Clock.fixed(Instant.parse("2026-10-15T00:00:00Z"), ZoneOffset.UTC));
        assertThat(service.nextApplicationReference()).isEqualTo("APP-2026-00001");
        assertThat(service.nextApplicationReference()).isEqualTo("APP-2026-00002");
    }

    @Test
    void createsCounterAndRecoversFromRace() {
        ReferenceCounter created = new ReferenceCounter("PER", 2026);
        when(repository.lockByKindAndYear("PER", 2026))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(created));
        when(repository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicate"));
        ReferenceNumberService service = new ReferenceNumberService(
                repository, Clock.fixed(Instant.parse("2026-10-15T00:00:00Z"), ZoneOffset.UTC));
        assertThat(service.nextPermitReference()).isEqualTo("PER-2026-00001");
    }

    @Test
    void savesWhenLockMisses() {
        ReferenceCounter created = new ReferenceCounter("APP", 2026);
        when(repository.lockByKindAndYear(anyString(), anyInt())).thenReturn(Optional.empty());
        when(repository.saveAndFlush(any())).thenReturn(created);
        ReferenceNumberService service = new ReferenceNumberService(
                repository, Clock.fixed(Instant.parse("2026-10-15T00:00:00Z"), ZoneOffset.UTC));
        assertThat(service.nextApplicationReference()).isEqualTo("APP-2026-00001");
    }

    @Test
    void counterKeyEquality() {
        ReferenceCounter.Key left = new ReferenceCounter.Key("APP", 2026);
        ReferenceCounter.Key right = new ReferenceCounter.Key("APP", 2026);
        assertThat(left).isEqualTo(right).hasSameHashCodeAs(right);
        assertThat(left).isNotEqualTo(new ReferenceCounter.Key("PER", 2026));
        assertThat(new ReferenceCounter.Key()).isNotEqualTo(left);
        assertThat(left.equals(left)).isTrue();
        assertThat(left.equals("APP")).isFalse();
        assertThat(new ReferenceCounter("APP", 2026).getLastValue()).isZero();
    }
}

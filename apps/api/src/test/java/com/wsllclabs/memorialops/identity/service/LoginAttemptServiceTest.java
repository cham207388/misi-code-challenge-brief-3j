package com.wsllclabs.memorialops.identity.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wsllclabs.memorialops.exception.RateLimitedException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class LoginAttemptServiceTest {

    @Test
    void fifthFailureStillAllowedSixthIsLimited() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-15T12:00:00Z"), ZoneOffset.UTC);
        LoginAttemptService service = new LoginAttemptService(clock);
        for (int i = 0; i < 5; i++) {
            service.guard("Coordinator");
            service.recordFailure("coordinator");
        }
        assertThatThrownBy(() -> service.guard("COORDINATOR")).isInstanceOf(RateLimitedException.class);
        service.recordSuccess("coordinator");
        assertThatCode(() -> service.guard("coordinator")).doesNotThrowAnyException();
    }
}

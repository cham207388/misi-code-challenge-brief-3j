package com.wsllclabs.memorialops.identity.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class SessionExpiryTest {

    @Test
    void usesSessionLastAccessAndTimeout() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession(true).setMaxInactiveInterval(1800);
        Instant expires = SessionExpiry.of(request);
        assertThat(expires).isAfter(Instant.now().minusSeconds(1));
        assertThat(expires).isBefore(Instant.now().plusSeconds(1801));
    }

    @Test
    void withoutSessionReturnsNow() {
        Instant expires = SessionExpiry.of(new MockHttpServletRequest());
        assertThat(expires).isBeforeOrEqualTo(Instant.now().plusSeconds(1));
    }
}

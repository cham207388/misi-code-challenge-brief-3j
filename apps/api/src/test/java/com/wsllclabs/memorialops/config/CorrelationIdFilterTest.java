package com.wsllclabs.memorialops.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void mintsIdWhenMissingOrUnsafe() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdFilter.HEADER, "not valid!!");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain =
                (req, res) -> assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNotBlank();
        filter.doFilter(request, response, chain);
        assertThat(response.getHeader(CorrelationIdFilter.HEADER)).isNotEqualTo("not valid!!");
        assertThat(response.getHeader(CorrelationIdFilter.HEADER)).matches(CorrelationIdFilter.VALID);
    }

    @Test
    void acceptsSafeIncomingId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdFilter.HEADER, "trace_ABC-123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> {});
        assertThat(response.getHeader(CorrelationIdFilter.HEADER)).isEqualTo("trace_ABC-123");
    }
}

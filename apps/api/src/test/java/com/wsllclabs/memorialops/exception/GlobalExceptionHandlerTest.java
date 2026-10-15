package com.wsllclabs.memorialops.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.wsllclabs.memorialops.config.CorrelationIdFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void unexpectedIncludesCorrelationId() {
        MDC.put(CorrelationIdFilter.MDC_KEY, "corr-500");
        ResponseEntity<ProblemDetail> response = handler.unexpected(new IllegalStateException("secret"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetail()).isEqualTo("An unexpected error occurred");
        assertThat(response.getBody().getProperties()).containsEntry("correlationId", "corr-500");
    }

    @Test
    void authAndConflictHandlersIncludeCorrelationId() {
        MDC.put(CorrelationIdFilter.MDC_KEY, "corr-auth");
        assertThat(handler.badCredentials().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(handler.authentication(
                                new org.springframework.security.authentication
                                        .AuthenticationCredentialsNotFoundException("x"))
                        .getBody()
                        .getProperties())
                .containsEntry("correlationId", "corr-auth");
        assertThat(handler.accessDenied().getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        InvalidTransitionException transition = new InvalidTransitionException("no", java.util.List.of("SUBMIT"));
        ResponseEntity<ProblemDetail> unprocessable = handler.invalidTransition(transition);
        assertThat(unprocessable.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(unprocessable.getBody().getProperties())
                .containsEntry("correlationId", "corr-auth")
                .containsEntry("allowedTransitions", java.util.List.of("SUBMIT"));
    }

    @Test
    void staleAndConstraintHandlersIncludeCorrelationId() {
        MDC.put(CorrelationIdFilter.MDC_KEY, "corr-more");
        ResponseEntity<ProblemDetail> stale =
                handler.stale(new org.springframework.orm.ObjectOptimisticLockingFailureException(String.class, "1"));
        assertThat(stale.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(stale.getBody().getProperties()).containsEntry("correlationId", "corr-more");
        ProblemDetail constraints =
                handler.constraintViolation(new jakarta.validation.ConstraintViolationException(java.util.Set.of()));
        assertThat(constraints.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(constraints.getProperties()).containsEntry("correlationId", "corr-more");
    }
}

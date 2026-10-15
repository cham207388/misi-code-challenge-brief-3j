package com.wsllclabs.memorialops.exception;

import com.wsllclabs.memorialops.config.CorrelationIdFilter;
import java.net.URI;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

public final class ProblemSupport {

    public static final String TYPE_PREFIX = "urn:memorial-ops:";

    private ProblemSupport() {}

    public static ProblemDetail create(HttpStatus status, String detail) {
        return create(status, typeFor(status), status.getReasonPhrase(), detail);
    }

    public static ProblemDetail create(HttpStatus status, String type, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create(type));
        problem.setTitle(title);
        problem.setProperty("correlationId", currentCorrelationId());
        return problem;
    }

    public static String currentCorrelationId() {
        String value = MDC.get(CorrelationIdFilter.MDC_KEY);
        return value == null ? "" : value;
    }

    public static String typeFor(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> TYPE_PREFIX + "validation";
            case UNAUTHORIZED -> TYPE_PREFIX + "unauthorized";
            case FORBIDDEN -> TYPE_PREFIX + "forbidden";
            case NOT_FOUND -> TYPE_PREFIX + "not-found";
            case CONFLICT -> TYPE_PREFIX + "conflict";
            case UNPROCESSABLE_CONTENT -> TYPE_PREFIX + "invalid-transition";
            case TOO_MANY_REQUESTS -> TYPE_PREFIX + "rate-limited";
            default -> TYPE_PREFIX + "error";
        };
    }
}

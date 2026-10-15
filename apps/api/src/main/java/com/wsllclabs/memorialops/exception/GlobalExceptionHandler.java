package com.wsllclabs.memorialops.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail problem = ProblemSupport.create(HttpStatus.BAD_REQUEST, "Request validation failed");
        problem.setProperty(
                "errors",
                ex.getBindingResult().getFieldErrors().stream()
                        .map(error -> error.getField() + ": " + error.getDefaultMessage())
                        .toList());
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail constraintViolation(ConstraintViolationException exception) {
        ProblemDetail problem = ProblemSupport.create(HttpStatus.BAD_REQUEST, "Request validation failed");
        problem.setProperty(
                "errors",
                exception.getConstraintViolations().stream()
                        .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                        .toList());
        return problem;
    }

    @ExceptionHandler({BadCredentialsException.class, DisabledException.class})
    ResponseEntity<ProblemDetail> badCredentials() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ProblemSupport.create(HttpStatus.UNAUTHORIZED, "Invalid username or password"));
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ProblemDetail> authentication(AuthenticationException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ProblemSupport.create(HttpStatus.UNAUTHORIZED, "Not authenticated"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ProblemDetail> accessDenied() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ProblemSupport.create(HttpStatus.FORBIDDEN, "Access denied"));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    ResponseEntity<ProblemDetail> stale(ObjectOptimisticLockingFailureException exception) {
        ProblemDetail problem = ProblemSupport.create(
                HttpStatus.CONFLICT,
                ProblemSupport.TYPE_PREFIX + "conflict",
                "Stale update",
                "The record was changed by someone else");
        problem.setProperty("entity", exception.getPersistentClassName());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .header(HttpHeaders.ETAG, "\"0\"")
                .body(problem);
    }

    @ExceptionHandler(ConflictException.class)
    ResponseEntity<ProblemDetail> conflict(ConflictException exception) {
        ProblemDetail problem = ProblemSupport.create(
                HttpStatus.CONFLICT, ProblemSupport.TYPE_PREFIX + "conflict", "Stale update", exception.getMessage());
        problem.setProperty("version", exception.getVersion());
        problem.setProperty("etag", exception.getEtag());
        problem.setProperty("current", exception.getCurrent());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .header(HttpHeaders.ETAG, exception.getEtag())
                .body(problem);
    }

    @ExceptionHandler(InvalidTransitionException.class)
    ResponseEntity<ProblemDetail> invalidTransition(InvalidTransitionException exception) {
        ProblemDetail problem = ProblemSupport.create(
                HttpStatus.UNPROCESSABLE_CONTENT,
                ProblemSupport.TYPE_PREFIX + "invalid-transition",
                "Invalid transition",
                exception.getMessage());
        problem.setProperty("allowedTransitions", exception.getAllowedTransitions());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(problem);
    }

    @ExceptionHandler(SeparationOfDutiesException.class)
    ResponseEntity<ProblemDetail> separationOfDuties(SeparationOfDutiesException exception) {
        ProblemDetail problem = ProblemSupport.create(
                HttpStatus.FORBIDDEN,
                ProblemSupport.TYPE_PREFIX + "separation-of-duties",
                "Separation of duties",
                exception.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
    }

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ProblemDetail> notFound(NotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ProblemSupport.create(HttpStatus.NOT_FOUND, exception.getMessage()));
    }

    @ExceptionHandler(RateLimitedException.class)
    ResponseEntity<ProblemDetail> rateLimited(RateLimitedException exception) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ProblemSupport.create(HttpStatus.TOO_MANY_REQUESTS, exception.getMessage()));
    }

    @ExceptionHandler(RequestValidationException.class)
    ResponseEntity<ProblemDetail> requestValidation(RequestValidationException exception) {
        return ResponseEntity.badRequest().body(ProblemSupport.create(HttpStatus.BAD_REQUEST, exception.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> unexpected(Exception exception) {
        ProblemDetail problem = ProblemSupport.create(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
}

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
                HttpStatus.CONFLICT, ProblemSupport.TYPE_PREFIX + "conflict", "Stale update", "The record was changed by someone else");
        problem.setProperty("entity", exception.getPersistentClassName());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
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
}

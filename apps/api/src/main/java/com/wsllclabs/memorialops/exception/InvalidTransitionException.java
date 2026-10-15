package com.wsllclabs.memorialops.exception;

import java.util.List;

public class InvalidTransitionException extends RuntimeException {

    private final List<String> allowedTransitions;

    public InvalidTransitionException(String message, List<String> allowedTransitions) {
        super(message);
        this.allowedTransitions = List.copyOf(allowedTransitions);
    }

    public List<String> getAllowedTransitions() {
        return allowedTransitions;
    }
}

package com.wsllclabs.memorialops.mocks.common;

import java.util.Arrays;

public enum ScenarioMode {
    SUCCESS,
    SLOW,
    UNAUTHORIZED,
    NOT_FOUND,
    CONFLICT,
    RATE_LIMITED,
    SERVER_ERROR,
    TIMEOUT,
    DUPLICATE_CALLBACK,
    OUT_OF_ORDER,
    BAD_SIGNATURE,
    OUTAGE;

    public static ScenarioMode fromToken(String token) {
        if (token == null || token.isBlank()) {
            return SUCCESS;
        }
        String normalized = token.trim().toUpperCase().replace('-', '_');
        return switch (normalized) {
            case "401", "UNAUTHORIZED" -> UNAUTHORIZED;
            case "404", "NOT_FOUND" -> NOT_FOUND;
            case "409", "CONFLICT" -> CONFLICT;
            case "429", "RATE_LIMITED" -> RATE_LIMITED;
            case "500", "SERVER_ERROR" -> SERVER_ERROR;
            default -> Arrays.stream(values())
                    .filter(mode -> mode.name().equals(normalized))
                    .findFirst()
                    .orElse(SUCCESS);
        };
    }
}

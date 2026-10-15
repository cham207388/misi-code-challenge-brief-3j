package com.wsllclabs.memorialops.identity.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.Instant;

public final class SessionExpiry {

    private SessionExpiry() {}

    public static Instant of(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return Instant.now();
        }
        return Instant.ofEpochMilli(session.getLastAccessedTime() + session.getMaxInactiveInterval() * 1000L);
    }
}

package com.wsllclabs.memorialops.identity.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MeResponse(
        UUID id,
        String username,
        String displayName,
        List<String> roles,
        List<String> capabilities,
        Instant sessionExpiresAt) {

    public MeResponse withSessionExpiry(Instant expiresAt) {
        return new MeResponse(id, username, displayName, roles, capabilities, expiresAt);
    }
}

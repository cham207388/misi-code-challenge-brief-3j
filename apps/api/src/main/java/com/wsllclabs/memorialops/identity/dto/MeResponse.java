package com.wsllclabs.memorialops.identity.dto;

import java.util.List;
import java.util.UUID;

public record MeResponse(UUID id, String username, String displayName, List<String> roles, List<String> capabilities) {}

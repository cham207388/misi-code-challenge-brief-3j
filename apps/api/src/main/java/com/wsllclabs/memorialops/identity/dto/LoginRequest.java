package com.wsllclabs.memorialops.identity.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String username, @NotBlank String password) {}

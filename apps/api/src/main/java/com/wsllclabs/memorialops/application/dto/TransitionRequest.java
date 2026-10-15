package com.wsllclabs.memorialops.application.dto;

import com.wsllclabs.memorialops.application.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TransitionRequest(
        @NotNull ApplicationStatus target, @Size(max = 2000) String reason, Long version) {}

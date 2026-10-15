package com.wsllclabs.memorialops.application.dto;

import com.wsllclabs.memorialops.application.ApplicationStatus;

public record AvailableAction(ApplicationStatus target, boolean enabled, String reason) {}

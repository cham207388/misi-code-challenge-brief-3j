package com.wsllclabs.memorialops.application.dto;

import java.util.UUID;

public record ApplicantView(UUID id, String fullName, String email, String phone, String organization) {}

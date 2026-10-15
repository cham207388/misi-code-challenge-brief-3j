package com.wsllclabs.memorialops.application.dto;

import jakarta.validation.constraints.Size;

public record CreateDraftRequest(@Size(max = 200) String applicantName) {}

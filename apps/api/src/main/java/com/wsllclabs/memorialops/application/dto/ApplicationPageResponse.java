package com.wsllclabs.memorialops.application.dto;

import java.util.List;

public record ApplicationPageResponse(
        List<ApplicationSummary> content, int page, int size, long totalElements, int totalPages) {}

package com.wsllclabs.memorialops.application.dto;

import java.time.Instant;
import java.util.UUID;

public record EventView(UUID id, String title, Instant startsAt, Instant endsAt, Integer attendeeCount, String notes) {}

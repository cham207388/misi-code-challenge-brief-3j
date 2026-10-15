package com.wsllclabs.memorialops.shared.health;

import java.util.Map;

public record HealthSummary(String status, Map<String, String> components) {}

package com.wsllclabs.memorialops.mocks.calendar;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class RegionalCalendarController {

    @PostMapping("/oauth/token")
    public Map<String, Object> token() {
        return Map.of("access_token", "mock-" + UUID.randomUUID(), "expires_in", 3600, "token_type", "Bearer");
    }

    @GetMapping("/events")
    public List<Map<String, Object>> events(@RequestParam String from, @RequestParam String to) {
        return List.of(Map.of(
                "externalId",
                "ext-1001",
                "venueCode",
                "WILLOW-LAWN",
                "startUtc",
                from,
                "endUtc",
                to,
                "modifiedUtc",
                Instant.now().toString()));
    }

    @PostMapping("/reservations")
    public Map<String, Object> reserve() {
        if (Instant.now().getEpochSecond() % 17 == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "slot taken");
        }
        return Map.of("reservationId", UUID.randomUUID().toString(), "status", "accepted");
    }
}

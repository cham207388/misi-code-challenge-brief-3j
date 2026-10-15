package com.wsllclabs.memorialops.mocks.weather;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WeatherAdvisoryController {

    @GetMapping("/forecast")
    public Map<String, Object> read(
            @PathVariable(required = false) String reference,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String venue,
            @RequestParam(required = false) String date) {
        return Map.of(
                "service", "weather-advisory",
                "reference", reference == null ? "" : reference,
                "query", q == null ? "" : q,
                "venue", venue == null ? "" : venue,
                "date", date == null ? "" : date,
                "status", "ok",
                "generatedAt", Instant.now().toString());
    }

    @PostMapping("/forecast")
    public Map<String, Object> write() {
        return Map.of(
                "service", "weather-advisory",
                "id", UUID.randomUUID().toString(),
                "status", "accepted",
                "items", List.of());
    }
}

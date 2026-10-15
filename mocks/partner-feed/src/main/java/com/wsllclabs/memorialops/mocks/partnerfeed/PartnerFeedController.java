package com.wsllclabs.memorialops.mocks.partnerfeed;

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
public class PartnerFeedController {

    @GetMapping("/batches")
    public Map<String, Object> read(
            @PathVariable(required = false) String reference,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String venue,
            @RequestParam(required = false) String date) {
        return Map.of(
                "service", "partner-feed",
                "reference", reference == null ? "" : reference,
                "query", q == null ? "" : q,
                "venue", venue == null ? "" : venue,
                "date", date == null ? "" : date,
                "status", "ok",
                "generatedAt", Instant.now().toString());
    }

    @PostMapping("/batches")
    public Map<String, Object> write() {
        return Map.of(
                "service", "partner-feed",
                "id", UUID.randomUUID().toString(),
                "status", "accepted",
                "items", List.of());
    }
}

package com.wsllclabs.memorialops.mocks.registry;

import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ApplicantRegistryController {

    @GetMapping("/api/applicants/{reference}")
    public Map<String, Object> lookup(
            @PathVariable String reference, @RequestHeader(value = "X-API-Key", required = false) String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "missing api key");
        }
        if (reference.toLowerCase().contains("unknown")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no record");
        }
        String status = reference.toLowerCase().contains("flag") ? "Flagged" : "Valid";
        return Map.of(
                "referenceId",
                reference,
                "matchedName",
                "Jordan Hale",
                "eligibilityStatus",
                status,
                "verifiedAt",
                Instant.now().toString());
    }
}

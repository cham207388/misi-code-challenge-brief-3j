package com.wsllclabs.memorialops.scheduling;

import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lookups")
public class LookupController {

    private final CemeteryRepository cemeteryRepository;
    private final VenueRepository venueRepository;
    private final EventCategoryRepository eventCategoryRepository;

    public LookupController(
            CemeteryRepository cemeteryRepository,
            VenueRepository venueRepository,
            EventCategoryRepository eventCategoryRepository) {
        this.cemeteryRepository = cemeteryRepository;
        this.venueRepository = venueRepository;
        this.eventCategoryRepository = eventCategoryRepository;
    }

    @GetMapping("/cemeteries")
    @PreAuthorize("isAuthenticated()")
    public List<LookupItem> cemeteries() {
        return cemeteryRepository.findAll().stream()
                .map(item -> new LookupItem(item.getId(), item.getCode(), item.getName()))
                .toList();
    }

    @GetMapping("/venues")
    @PreAuthorize("isAuthenticated()")
    public List<VenueItem> venues(@RequestParam(required = false) UUID cemeteryId) {
        List<Venue> venues =
                cemeteryId == null ? venueRepository.findAll() : venueRepository.findByCemeteryId(cemeteryId);
        return venues.stream()
                .map(item -> new VenueItem(
                        item.getId(), item.getCemeteryId(), item.getCode(), item.getName(), item.getCapacity()))
                .toList();
    }

    @GetMapping("/event-categories")
    @PreAuthorize("isAuthenticated()")
    public List<LookupItem> eventCategories() {
        return eventCategoryRepository.findAll().stream()
                .map(item -> new LookupItem(item.getId(), item.getCode(), item.getName()))
                .toList();
    }

    public record LookupItem(UUID id, String code, String name) {}

    public record VenueItem(UUID id, UUID cemeteryId, String code, String name, int capacity) {}
}

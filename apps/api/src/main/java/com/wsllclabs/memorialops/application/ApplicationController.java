package com.wsllclabs.memorialops.application;

import com.wsllclabs.memorialops.application.dto.ApplicationDetail;
import com.wsllclabs.memorialops.application.dto.ApplicationPageResponse;
import com.wsllclabs.memorialops.application.dto.CreateDraftRequest;
import com.wsllclabs.memorialops.application.dto.UpdateDraftRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApplicationPageResponse list(
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return applicationService.list(status, page, size);
    }

    @PostMapping
    @PreAuthorize("hasRole('INTAKE_COORDINATOR')")
    public ResponseEntity<ApplicationDetail> create(@Valid @RequestBody(required = false) CreateDraftRequest request) {
        ApplicationDetail detail =
                applicationService.createDraft(request == null ? new CreateDraftRequest(null) : request);
        return ResponseEntity.status(201)
                .header(HttpHeaders.ETAG, detail.etag())
                .body(detail);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApplicationDetail> get(@PathVariable UUID id) {
        ApplicationDetail detail = applicationService.get(id);
        return ResponseEntity.ok().header(HttpHeaders.ETAG, detail.etag()).body(detail);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('INTAKE_COORDINATOR')")
    public ResponseEntity<ApplicationDetail> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDraftRequest request,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch) {
        ApplicationDetail detail = applicationService.updateDraft(id, request, ifMatch);
        return ResponseEntity.ok().header(HttpHeaders.ETAG, detail.etag()).body(detail);
    }
}

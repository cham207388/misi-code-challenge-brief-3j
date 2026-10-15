package com.wsllclabs.memorialops.workflow;

import com.wsllclabs.memorialops.application.dto.ApplicationDetail;
import com.wsllclabs.memorialops.application.dto.AvailableAction;
import com.wsllclabs.memorialops.application.dto.TransitionRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applications/{id}")
public class WorkflowController {

    private final WorkflowService workflowService;

    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @GetMapping("/actions")
    @PreAuthorize("isAuthenticated()")
    public List<AvailableAction> actions(@PathVariable UUID id) {
        return workflowService.actions(id);
    }

    @PostMapping("/transitions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApplicationDetail> transition(
            @PathVariable UUID id,
            @Valid @RequestBody TransitionRequest request,
            @RequestHeader(value = HttpHeaders.IF_MATCH, required = false) String ifMatch) {
        ApplicationDetail detail = workflowService.transition(id, request, ifMatch);
        return ResponseEntity.ok().header(HttpHeaders.ETAG, detail.etag()).body(detail);
    }
}

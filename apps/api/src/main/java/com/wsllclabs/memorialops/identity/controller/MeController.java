package com.wsllclabs.memorialops.identity.controller;

import com.wsllclabs.memorialops.identity.dto.MeResponse;
import com.wsllclabs.memorialops.identity.service.CurrentUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
public class MeController {

    private final CurrentUserService currentUserService;

    public MeController(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public MeResponse me() {
        return currentUserService.current();
    }
}

package com.wsllclabs.memorialops.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaFallbackController {

    @GetMapping({
        "/",
        "/login",
        "/403",
        "/404",
        "/dashboard",
        "/applications",
        "/applications/**",
        "/reviews",
        "/information-requests",
        "/calendar",
        "/venues/**",
        "/resources",
        "/approvals",
        "/permits",
        "/notifications",
        "/roster",
        "/check-in",
        "/incidents",
        "/reports",
        "/admin/**",
        "/integrations",
        "/integrations/**",
        "/documents"
    })
    public String spa() {
        return "forward:/index.html";
    }
}

package com.wsllclabs.memorialops.identity.controller;

import com.wsllclabs.memorialops.identity.dto.CsrfResponse;
import com.wsllclabs.memorialops.identity.dto.LoginRequest;
import com.wsllclabs.memorialops.identity.dto.MeResponse;
import com.wsllclabs.memorialops.identity.service.CurrentUserService;
import com.wsllclabs.memorialops.identity.service.LoginAttemptService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CurrentUserService currentUserService;
    private final SessionAuthenticationStrategy sessionAuthenticationStrategy;
    private final SecurityContextRepository securityContextRepository;
    private final LoginAttemptService loginAttemptService;

    public AuthController(
            AuthenticationManager authenticationManager,
            CurrentUserService currentUserService,
            SessionAuthenticationStrategy sessionAuthenticationStrategy,
            SecurityContextRepository securityContextRepository,
            LoginAttemptService loginAttemptService) {
        this.authenticationManager = authenticationManager;
        this.currentUserService = currentUserService;
        this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
        this.securityContextRepository = securityContextRepository;
        this.loginAttemptService = loginAttemptService;
    }

    @GetMapping("/csrf")
    @PreAuthorize("permitAll()")
    public CsrfResponse csrf(CsrfToken csrfToken, HttpServletRequest request) {
        String token = csrfToken.getToken();
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if ("XSRF-TOKEN".equals(cookie.getName())
                        && cookie.getValue() != null
                        && !cookie.getValue().isBlank()) {
                    token = cookie.getValue();
                    break;
                }
            }
        }
        return new CsrfResponse(csrfToken.getHeaderName(), token);
    }

    @PostMapping("/login")
    @PreAuthorize("permitAll()")
    public MeResponse login(
            @Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        loginAttemptService.guard(loginRequest.username());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);
            sessionAuthenticationStrategy.onAuthentication(authentication, request, response);
            securityContextRepository.saveContext(securityContext, request, response);
            loginAttemptService.recordSuccess(loginRequest.username());
            return currentUserService.forUsername(authentication.getName());
        } catch (BadCredentialsException | DisabledException exception) {
            loginAttemptService.recordFailure(loginRequest.username());
            throw exception;
        }
    }
}

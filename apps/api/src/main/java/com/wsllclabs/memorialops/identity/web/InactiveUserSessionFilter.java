package com.wsllclabs.memorialops.identity.web;

import com.wsllclabs.memorialops.exception.ProblemSupport;
import com.wsllclabs.memorialops.identity.entity.AppUser;
import com.wsllclabs.memorialops.identity.repository.AppUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class InactiveUserSessionFilter extends OncePerRequestFilter {

    private final AppUserRepository appUserRepository;

    public InactiveUserSessionFilter(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/")
                || path.startsWith("/api/auth/")
                || path.equals("/api/health")
                || path.startsWith("/api/health/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            filterChain.doFilter(request, response);
            return;
        }
        AppUser user = appUserRepository
                .findByUsernameIgnoreCaseAndDeletedAtIsNull(authentication.getName())
                .orElse(null);
        if (user != null && user.isActive()) {
            filterChain.doFilter(request, response);
            return;
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        writeUnauthorized(response);
    }

    private static void writeUnauthorized(HttpServletResponse response) throws IOException {
        String correlationId = ProblemSupport.currentCorrelationId();
        String body = """
                {"type":"%s","title":"Unauthorized","status":401,"detail":"Not authenticated","correlationId":"%s"}
                """.formatted(ProblemSupport.typeFor(HttpStatus.UNAUTHORIZED), correlationId);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
    }
}

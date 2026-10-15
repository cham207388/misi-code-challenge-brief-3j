package com.wsllclabs.memorialops.mocks.common;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class ScenarioFilter extends OncePerRequestFilter {

    private final ScenarioStore scenarioStore;
    private final CallLog callLog;

    public ScenarioFilter(ScenarioStore scenarioStore, CallLog callLog) {
        this.scenarioStore = scenarioStore;
        this.callLog = callLog;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path.startsWith("/__admin") || path.startsWith("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }
        ScenarioMode mode = scenarioStore.current();
        switch (mode) {
            case SLOW -> sleep(1500);
            case TIMEOUT -> {
                sleep(8000);
                response.setStatus(504);
                write(response, "timeout");
                callLog.add(request.getMethod(), path, 504);
                return;
            }
            case OUTAGE -> {
                response.setStatus(503);
                write(response, "outage");
                callLog.add(request.getMethod(), path, 503);
                return;
            }
            case UNAUTHORIZED -> {
                response.setStatus(401);
                write(response, "unauthorized");
                callLog.add(request.getMethod(), path, 401);
                return;
            }
            case NOT_FOUND -> {
                response.setStatus(404);
                write(response, "not-found");
                callLog.add(request.getMethod(), path, 404);
                return;
            }
            case CONFLICT -> {
                response.setStatus(409);
                write(response, "conflict");
                callLog.add(request.getMethod(), path, 409);
                return;
            }
            case RATE_LIMITED -> {
                response.setStatus(429);
                response.setHeader("Retry-After", "2");
                write(response, "rate-limited");
                callLog.add(request.getMethod(), path, 429);
                return;
            }
            case SERVER_ERROR -> {
                response.setStatus(500);
                write(response, "server-error");
                callLog.add(request.getMethod(), path, 500);
                return;
            }
            default -> {
                // success and remaining demo modes fall through
            }
        }
        StatusCaptureResponse wrapper = new StatusCaptureResponse(response);
        filterChain.doFilter(request, wrapper);
        callLog.add(request.getMethod(), path, wrapper.status());
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private static void write(HttpServletResponse response, String code) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"error\":\"" + code + "\"}");
    }
}

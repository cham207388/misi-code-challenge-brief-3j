package com.wsllclabs.memorialops.identity.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wsllclabs.memorialops.identity.entity.AppUser;
import com.wsllclabs.memorialops.identity.repository.AppUserRepository;
import jakarta.servlet.FilterChain;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class InactiveUserSessionFilterTest {

    private final AppUserRepository users = Mockito.mock(AppUserRepository.class);
    private final InactiveUserSessionFilter filter = new InactiveUserSessionFilter(users);

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void skipsPublicAuthPaths() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/login");
        request.setRequestURI("/api/auth/login");
        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    @Test
    void revokesInactiveUserSession() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/me");
        request.setRequestURI("/api/me");
        request.getSession(true);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("coordinator", "n/a", List.of()));
        AppUser inactive = Mockito.mock(AppUser.class);
        when(inactive.isActive()).thenReturn(false);
        when(users.findByUsernameIgnoreCaseAndDeletedAtIsNull("coordinator")).thenReturn(Optional.of(inactive));

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(request.getSession(false)).isNull();
        verify(chain, Mockito.never()).doFilter(request, response);
    }

    @Test
    void continuesWhenUserIsActive() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/me");
        request.setRequestURI("/api/me");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = Mockito.mock(FilterChain.class);
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("coordinator", "n/a", List.of()));
        AppUser active = Mockito.mock(AppUser.class);
        when(active.isActive()).thenReturn(true);
        when(users.findByUsernameIgnoreCaseAndDeletedAtIsNull("coordinator")).thenReturn(Optional.of(active));

        filter.doFilterInternal(request, response, chain);

        verify(users).findByUsernameIgnoreCaseAndDeletedAtIsNull("coordinator");
        verify(chain).doFilter(request, response);
    }
}

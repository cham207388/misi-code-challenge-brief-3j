package com.wsllclabs.memorialops;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wsllclabs.memorialops.support.AbstractIntegrationTest;
import com.wsllclabs.memorialops.support.LoginRequestBuilder;
import com.wsllclabs.memorialops.support.TestUsers;
import com.wsllclabs.memorialops.support.UserSession;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MvcResult;

class MemorialOpsIT extends AbstractIntegrationTest {

    @Test
    void healthLoginAndSpaFallback() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Correlation-ID"))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components.db").value("UP"));

        mockMvc.perform(get("/actuator/health/liveness")).andExpect(status().isOk());
        mockMvc.perform(get("/actuator/health/readiness")).andExpect(status().isOk());

        UserSession.Authenticated session =
                UserSession.login(mockMvc, TestUsers.COORDINATOR.username(), TestUsers.COORDINATOR.password());
        mockMvc.perform(get("/api/me").session(session.session()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("coordinator"))
                .andExpect(jsonPath("$.sessionExpiresAt").exists());

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LoginRequestBuilder.forUser("coordinator")
                                .password("wrong")
                                .json()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/auth/csrf").cookie(new jakarta.servlet.http.Cookie("XSRF-TOKEN", "cookie-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("cookie-token"));

        MvcResult spa = mockMvc.perform(get("/applications/demo-case"))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(spa.getResponse().getForwardedUrl()).isEqualTo("/index.html");
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Memorial Event Operations")));
    }

    @Test
    void loginChangesSessionId() throws Exception {
        MockHttpSession before = UserSession.csrfSession(mockMvc);
        String previousId = before.getId();
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .session(before)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LoginRequestBuilder.forUser(TestUsers.COORDINATOR.username())
                                .password(TestUsers.COORDINATOR.password())
                                .json()))
                .andExpect(status().isOk())
                .andReturn();
        MockHttpSession after = (MockHttpSession) login.getRequest().getSession(false);
        assertThat(after).isNotNull();
        assertThat(after.getId()).isNotEqualTo(previousId);
    }

    @Test
    void dualRoleUserHasReviewAndApprovalCapabilities() throws Exception {
        UserSession.Authenticated session =
                UserSession.login(mockMvc, TestUsers.DUALROLE.username(), TestUsers.DUALROLE.password());
        mockMvc.perform(get("/api/me").session(session.session()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles")
                        .value(org.hamcrest.Matchers.hasItems("ELIGIBILITY_REVIEWER", "OPERATIONS_MANAGER")))
                .andExpect(jsonPath("$.capabilities")
                        .value(org.hamcrest.Matchers.hasItems("REVIEW_QUEUE", "APPROVAL_DECIDE")));
    }

    @Test
    void rejectsUnsafeCorrelationIds() throws Exception {
        mockMvc.perform(get("/api/health").header("X-Correlation-ID", "bad id with spaces"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-ID", org.hamcrest.Matchers.not("bad id with spaces")));
        mockMvc.perform(get("/api/health").header("X-Correlation-ID", "ok-id_123"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-ID", "ok-id_123"));
    }
}

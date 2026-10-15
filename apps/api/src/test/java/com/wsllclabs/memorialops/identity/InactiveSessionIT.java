package com.wsllclabs.memorialops.identity;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wsllclabs.memorialops.support.AbstractIntegrationTest;
import com.wsllclabs.memorialops.support.TestUsers;
import com.wsllclabs.memorialops.support.UserSession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class InactiveSessionIT extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void inactiveUserSessionIsRevoked() throws Exception {
        UserSession.Authenticated session =
                UserSession.login(mockMvc, TestUsers.COORDINATOR.username(), TestUsers.COORDINATOR.password());
        try {
            jdbcTemplate.update(
                    "update app_user set active = false where username = ?", TestUsers.COORDINATOR.username());
            mockMvc.perform(get("/api/me").session(session.session())).andExpect(status().isUnauthorized());
        } finally {
            jdbcTemplate.update(
                    "update app_user set active = true where username = ?", TestUsers.COORDINATOR.username());
        }
        UserSession.Authenticated restored =
                UserSession.login(mockMvc, TestUsers.COORDINATOR.username(), TestUsers.COORDINATOR.password());
        mockMvc.perform(get("/api/me").session(restored.session())).andExpect(status().isOk());
    }
}

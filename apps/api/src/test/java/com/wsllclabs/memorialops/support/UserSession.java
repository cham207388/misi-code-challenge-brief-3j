package com.wsllclabs.memorialops.support;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

public final class UserSession {

    public record Authenticated(MockHttpSession session, String username) {}

    private UserSession() {}

    public static MockHttpSession csrfSession(MockMvc mockMvc) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(true);
    }

    public static Authenticated login(MockMvc mockMvc, String username, String password) throws Exception {
        MockHttpSession session = csrfSession(mockMvc);
        mockMvc.perform(post("/api/auth/login")
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LoginRequestBuilder.forUser(username)
                                .password(password)
                                .json()))
                .andExpect(status().isOk());
        return new Authenticated(session, username);
    }
}

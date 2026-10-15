package com.wsllclabs.memorialops.support;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

public final class CaseHttp {

    public static final String CEMETERY = "44444444-4444-4444-4444-444444444001";
    public static final String VENUE = "55555555-5555-5555-5555-555555555001";
    public static final String CATEGORY = "66666666-6666-6666-6666-666666666001";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private CaseHttp() {}

    public static JsonNode createDraft(MockMvc mockMvc, UserSession.Authenticated session) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/applications")
                        .session(session.session())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"applicantName\":\"Jordan Hale\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return JSON.readTree(result.getResponse().getContentAsString());
    }

    public static JsonNode completeDraft(MockMvc mockMvc, UserSession.Authenticated session, JsonNode current)
            throws Exception {
        String body =
                """
                {
                  "applicantName":"Jordan Hale",
                  "applicantEmail":"jordan.hale@example.com",
                  "applicantPhone":"555-0100",
                  "organization":"Hale Family",
                  "cemeteryId":"%s",
                  "venueId":"%s",
                  "eventCategoryId":"%s",
                  "eventTitle":"Sunrise remembrance",
                  "startsAt":"2026-11-01T15:00:00Z",
                  "endsAt":"2026-11-01T16:30:00Z",
                  "attendeeCount":40,
                  "wizardStep":4,
                  "attachmentReady":true,
                  "version":%d
                }
                """.formatted(CEMETERY, VENUE, CATEGORY, current.get("version").asLong());
        MvcResult result = mockMvc.perform(patch(
                                "/api/applications/" + current.get("id").asString())
                        .session(session.session())
                        .with(csrf())
                        .header(HttpHeaders.IF_MATCH, current.get("etag").asString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        return JSON.readTree(result.getResponse().getContentAsString());
    }

    public static JsonNode transition(
            MockMvc mockMvc, UserSession.Authenticated session, JsonNode current, String target, String reason)
            throws Exception {
        String payload = reason == null
                ? "{\"target\":\"%s\",\"version\":%d}"
                        .formatted(target, current.get("version").asLong())
                : "{\"target\":\"%s\",\"reason\":%s,\"version\":%d}"
                        .formatted(
                                target,
                                JSON.writeValueAsString(reason),
                                current.get("version").asLong());
        MvcResult result = mockMvc.perform(post("/api/applications/"
                                + current.get("id").asString() + "/transitions")
                        .session(session.session())
                        .with(csrf())
                        .header(HttpHeaders.IF_MATCH, current.get("etag").asString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn();
        return JSON.readTree(result.getResponse().getContentAsString());
    }

    public static JsonNode fetch(MockMvc mockMvc, UserSession.Authenticated session, String id) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/applications/" + id).session(session.session()))
                .andExpect(status().isOk())
                .andReturn();
        return JSON.readTree(result.getResponse().getContentAsString());
    }
}

package com.wsllclabs.memorialops.application;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wsllclabs.memorialops.support.AbstractIntegrationTest;
import com.wsllclabs.memorialops.support.CaseHttp;
import com.wsllclabs.memorialops.support.TestUsers;
import com.wsllclabs.memorialops.support.UserSession;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import tools.jackson.databind.JsonNode;

class ApplicationWorkflowIT extends AbstractIntegrationTest {

    @Test
    void draftSubmitReviewReserveApproveAndConflict() throws Exception {
        UserSession.Authenticated coordinator =
                UserSession.login(mockMvc, TestUsers.COORDINATOR.username(), TestUsers.COORDINATOR.password());
        JsonNode created = CaseHttp.createDraft(mockMvc, coordinator);
        mockMvc.perform(patch("/api/applications/" + created.get("id").asString())
                        .session(coordinator.session())
                        .with(csrf())
                        .header(HttpHeaders.IF_MATCH, "\"99\"")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"wizardStep\":2,\"version\":99}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("urn:memorial-ops:conflict"))
                .andExpect(jsonPath("$.version").exists())
                .andExpect(jsonPath("$.etag").exists())
                .andExpect(jsonPath("$.current.reference").exists());
        JsonNode draft = CaseHttp.completeDraft(mockMvc, coordinator, created);
        mockMvc.perform(get("/api/applications/" + draft.get("id").asString()).session(coordinator.session()))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.ETAG))
                .andExpect(jsonPath("$.status").value("DRAFT"));

        JsonNode submitted = CaseHttp.transition(mockMvc, coordinator, draft, "SUBMITTED", null);
        mockMvc.perform(get("/api/applications/" + submitted.get("id").asString() + "/actions")
                        .session(coordinator.session()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].target").exists());

        UserSession.Authenticated reviewer =
                UserSession.login(mockMvc, TestUsers.REVIEWER.username(), TestUsers.REVIEWER.password());
        JsonNode eligible = CaseHttp.transition(mockMvc, reviewer, submitted, "ELIGIBLE", null);

        UserSession.Authenticated scheduler =
                UserSession.login(mockMvc, TestUsers.SCHEDULER.username(), TestUsers.SCHEDULER.password());
        JsonNode reserved = CaseHttp.transition(mockMvc, scheduler, eligible, "TENTATIVELY_RESERVED", null);
        JsonNode pending = CaseHttp.transition(mockMvc, scheduler, reserved, "PENDING_APPROVAL", null);

        UserSession.Authenticated manager =
                UserSession.login(mockMvc, TestUsers.MANAGER.username(), TestUsers.MANAGER.password());
        CaseHttp.transition(mockMvc, manager, pending, "APPROVED", null);
    }

    @Test
    void invalidTransitionAndMissingReason() throws Exception {
        UserSession.Authenticated coordinator =
                UserSession.login(mockMvc, TestUsers.COORDINATOR.username(), TestUsers.COORDINATOR.password());
        JsonNode draft = CaseHttp.createDraft(mockMvc, coordinator);
        mockMvc.perform(post("/api/applications/" + draft.get("id").asString() + "/transitions")
                        .session(coordinator.session())
                        .with(csrf())
                        .header(HttpHeaders.IF_MATCH, draft.get("etag").asString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"target\":\"APPROVED\",\"version\":0}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.type").value("urn:memorial-ops:invalid-transition"))
                .andExpect(jsonPath("$.allowedTransitions").isArray());

        JsonNode ready = CaseHttp.completeDraft(mockMvc, coordinator, draft);
        JsonNode submitted = CaseHttp.transition(mockMvc, coordinator, ready, "SUBMITTED", null);
        UserSession.Authenticated reviewer =
                UserSession.login(mockMvc, TestUsers.REVIEWER.username(), TestUsers.REVIEWER.password());
        mockMvc.perform(post("/api/applications/" + submitted.get("id").asString() + "/transitions")
                        .session(reviewer.session())
                        .with(csrf())
                        .header(HttpHeaders.IF_MATCH, submitted.get("etag").asString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"target\":\"REJECTED\",\"version\":"
                                + submitted.get("version").asLong()
                                + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("A reason is required for this transition"));
    }

    @Test
    void separationOfDutiesAndAdminCannotApprove() throws Exception {
        UserSession.Authenticated coordinator =
                UserSession.login(mockMvc, TestUsers.COORDINATOR.username(), TestUsers.COORDINATOR.password());
        JsonNode submitted = CaseHttp.transition(
                mockMvc,
                coordinator,
                CaseHttp.completeDraft(mockMvc, coordinator, CaseHttp.createDraft(mockMvc, coordinator)),
                "SUBMITTED",
                null);
        UserSession.Authenticated dual =
                UserSession.login(mockMvc, TestUsers.DUALROLE.username(), TestUsers.DUALROLE.password());
        JsonNode eligible = CaseHttp.transition(mockMvc, dual, submitted, "ELIGIBLE", null);
        UserSession.Authenticated scheduler =
                UserSession.login(mockMvc, TestUsers.SCHEDULER.username(), TestUsers.SCHEDULER.password());
        JsonNode pending = CaseHttp.transition(
                mockMvc,
                scheduler,
                CaseHttp.transition(mockMvc, scheduler, eligible, "TENTATIVELY_RESERVED", null),
                "PENDING_APPROVAL",
                null);
        mockMvc.perform(post("/api/applications/" + pending.get("id").asString() + "/transitions")
                        .session(dual.session())
                        .with(csrf())
                        .header(HttpHeaders.IF_MATCH, pending.get("etag").asString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"target\":\"APPROVED\",\"version\":"
                                + pending.get("version").asLong()
                                + "}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.type").value("urn:memorial-ops:separation-of-duties"));

        UserSession.Authenticated admin =
                UserSession.login(mockMvc, TestUsers.ADMIN.username(), TestUsers.ADMIN.password());
        mockMvc.perform(post("/api/applications/" + pending.get("id").asString() + "/transitions")
                        .session(admin.session())
                        .with(csrf())
                        .header(HttpHeaders.IF_MATCH, pending.get("etag").asString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"target\":\"APPROVED\",\"version\":"
                                + pending.get("version").asLong()
                                + "}"))
                .andExpect(status().isForbidden());
    }
}

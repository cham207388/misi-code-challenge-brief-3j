package com.wsllclabs.memorialops.identity;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.wsllclabs.memorialops.support.AbstractIntegrationTest;
import com.wsllclabs.memorialops.support.CaseHttp;
import com.wsllclabs.memorialops.support.TestUsers;
import com.wsllclabs.memorialops.support.UserSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import tools.jackson.databind.JsonNode;

class AuthorizationMatrixIT extends AbstractIntegrationTest {

    private String caseId;
    private String etag;

    @BeforeEach
    void seedCase() throws Exception {
        UserSession.Authenticated coordinator =
                UserSession.login(mockMvc, TestUsers.COORDINATOR.username(), TestUsers.COORDINATOR.password());
        JsonNode draft = CaseHttp.createDraft(mockMvc, coordinator);
        caseId = draft.get("id").asString();
        etag = draft.get("etag").asString();
    }

    @Test
    void anonymousIsUnauthorizedExceptPublicEndpoints() throws Exception {
        mockMvc.perform(get("/api/health")).andExpect(status().isOk());
        mockMvc.perform(get("/api/auth/csrf")).andExpect(status().isOk());
        mockMvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/applications")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/applications/" + caseId)).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/applications/" + caseId + "/actions")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/lookups/cemeteries")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/applications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void coordinatorCanCreateAndReadOwnCase() throws Exception {
        UserSession.Authenticated session =
                UserSession.login(mockMvc, TestUsers.COORDINATOR.username(), TestUsers.COORDINATOR.password());
        expect(session.session(), 200, 201, 200, 200, 200, 200);
    }

    @Test
    void reviewerCanReadButNotCreate() throws Exception {
        UserSession.Authenticated session =
                UserSession.login(mockMvc, TestUsers.REVIEWER.username(), TestUsers.REVIEWER.password());
        expect(session.session(), 200, 403, 200, 403, 200, 200);
    }

    @Test
    void schedulerCanReadEligiblePlusButNotCreate() throws Exception {
        UserSession.Authenticated session =
                UserSession.login(mockMvc, TestUsers.SCHEDULER.username(), TestUsers.SCHEDULER.password());
        mockMvc.perform(get("/api/applications").session(session.session())).andExpect(status().isOk());
        mockMvc.perform(post("/api/applications")
                        .session(session.session())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/applications/" + caseId).session(session.session()))
                .andExpect(status().isForbidden());
    }

    @Test
    void managerCanReadButNotCreate() throws Exception {
        UserSession.Authenticated session =
                UserSession.login(mockMvc, TestUsers.MANAGER.username(), TestUsers.MANAGER.password());
        expect(session.session(), 200, 403, 200, 403, 200, 200);
    }

    @Test
    void attendantCannotReadUnassignedCase() throws Exception {
        UserSession.Authenticated session =
                UserSession.login(mockMvc, TestUsers.ATTENDANT.username(), TestUsers.ATTENDANT.password());
        mockMvc.perform(get("/api/applications").session(session.session())).andExpect(status().isOk());
        mockMvc.perform(get("/api/applications/" + caseId).session(session.session()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("Access denied"))
                .andExpect(jsonPath("$.applicant").doesNotExist());
        mockMvc.perform(post("/api/applications")
                        .session(session.session())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void auditorSeesRedactedApplicant() throws Exception {
        UserSession.Authenticated session =
                UserSession.login(mockMvc, TestUsers.AUDITOR.username(), TestUsers.AUDITOR.password());
        mockMvc.perform(get("/api/applications/" + caseId).session(session.session()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicant.fullName").value("Redacted"))
                .andExpect(jsonPath("$.applicant.email").doesNotExist());
        mockMvc.perform(post("/api/applications")
                        .session(session.session())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCannotReadCases() throws Exception {
        UserSession.Authenticated session =
                UserSession.login(mockMvc, TestUsers.ADMIN.username(), TestUsers.ADMIN.password());
        mockMvc.perform(get("/api/applications").session(session.session())).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/applications/" + caseId).session(session.session()))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/lookups/cemeteries").session(session.session()))
                .andExpect(status().isOk());
    }

    private void expect(
            MockHttpSession session,
            int listStatus,
            int createStatus,
            int getStatus,
            int patchStatus,
            int actionsStatus,
            int lookupsStatus)
            throws Exception {
        mockMvc.perform(get("/api/applications").session(session)).andExpect(status().is(listStatus));
        mockMvc.perform(post("/api/applications")
                        .session(session)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().is(createStatus));
        mockMvc.perform(get("/api/applications/" + caseId).session(session)).andExpect(status().is(getStatus));
        mockMvc.perform(patch("/api/applications/" + caseId)
                        .session(session)
                        .with(csrf())
                        .header(HttpHeaders.IF_MATCH, etag)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"wizardStep\":2,\"version\":0}"))
                .andExpect(status().is(patchStatus));
        mockMvc.perform(get("/api/applications/" + caseId + "/actions").session(session))
                .andExpect(status().is(actionsStatus));
        mockMvc.perform(get("/api/lookups/venues").session(session)).andExpect(status().is(lookupsStatus));
    }
}

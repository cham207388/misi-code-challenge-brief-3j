package com.wsllclabs.memorialops.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wsllclabs.memorialops.application.ApplicationCaseRepository;
import com.wsllclabs.memorialops.application.StatusHistoryRepository;
import com.wsllclabs.memorialops.support.AbstractIntegrationTest;
import com.wsllclabs.memorialops.support.CaseHttp;
import com.wsllclabs.memorialops.support.TestUsers;
import com.wsllclabs.memorialops.support.UserSession;
import com.wsllclabs.memorialops.workflow.WorkflowAfterWriteHook;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.JsonNode;

@Import(AuditPersistenceIT.RollbackHookConfig.class)
class AuditPersistenceIT extends AbstractIntegrationTest {

    static final ThreadLocal<Boolean> FAIL = ThreadLocal.withInitial(() -> false);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Autowired
    private StatusHistoryRepository statusHistoryRepository;

    @Autowired
    private ApplicationCaseRepository applicationCaseRepository;

    @Test
    void auditTriggerBlocksUpdateAndDelete() {
        UUID id = jdbcTemplate.queryForObject(
                "insert into audit_event (id, occurred_at, action, entity_type) values (?::uuid, now(), 'PROBE', 'Test') returning id",
                UUID.class,
                UUID.randomUUID());
        assertThatThrownBy(() -> jdbcTemplate.update("update audit_event set action = 'X' where id = ?", id))
                .hasMessageContaining("append-only");
        assertThatThrownBy(() -> jdbcTemplate.update("delete from audit_event where id = ?", id))
                .hasMessageContaining("append-only");
    }

    @Test
    void statusAndAuditCommitTogetherAndRollBackTogether() throws Exception {
        UserSession.Authenticated coordinator =
                UserSession.login(mockMvc, TestUsers.COORDINATOR.username(), TestUsers.COORDINATOR.password());
        JsonNode ready = CaseHttp.completeDraft(mockMvc, coordinator, CaseHttp.createDraft(mockMvc, coordinator));
        UUID id = UUID.fromString(ready.get("id").asString());
        int audits = auditEventRepository
                .findByEntityTypeAndEntityIdOrderByOccurredAtAsc("Application", id)
                .size();
        int history = statusHistoryRepository
                .findByApplicationIdOrderByOccurredAtAsc(id)
                .size();

        CaseHttp.transition(mockMvc, coordinator, ready, "SUBMITTED", null);
        assertThat(auditEventRepository
                        .findByEntityTypeAndEntityIdOrderByOccurredAtAsc("Application", id)
                        .size())
                .isGreaterThan(audits);
        assertThat(statusHistoryRepository
                        .findByApplicationIdOrderByOccurredAtAsc(id)
                        .size())
                .isGreaterThan(history);
        assertThat(applicationCaseRepository
                        .findById(id)
                        .orElseThrow()
                        .getStatus()
                        .name())
                .isEqualTo("SUBMITTED");

        JsonNode created = CaseHttp.completeDraft(mockMvc, coordinator, CaseHttp.createDraft(mockMvc, coordinator));
        UUID rollbackId = UUID.fromString(created.get("id").asString());
        long rollbackAudits = auditEventRepository
                .findByEntityTypeAndEntityIdOrderByOccurredAtAsc("Application", rollbackId)
                .size();
        FAIL.set(true);
        try {
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(
                                    "/api/applications/" + rollbackId + "/transitions")
                            .session(coordinator.session())
                            .with(org.springframework.security.test.web.servlet.request
                                    .SecurityMockMvcRequestPostProcessors.csrf())
                            .header("If-Match", created.get("etag").asString())
                            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                            .content("{\"target\":\"SUBMITTED\",\"version\":"
                                    + created.get("version").asLong()
                                    + "}"))
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status()
                            .isInternalServerError());
        } finally {
            FAIL.set(false);
        }
        assertThat(applicationCaseRepository
                        .findById(rollbackId)
                        .orElseThrow()
                        .getStatus()
                        .name())
                .isEqualTo("DRAFT");
        assertThat(auditEventRepository
                        .findByEntityTypeAndEntityIdOrderByOccurredAtAsc("Application", rollbackId)
                        .size())
                .isEqualTo(rollbackAudits);
    }

    @TestConfiguration
    static class RollbackHookConfig {
        @Bean
        @Primary
        WorkflowAfterWriteHook failingHook() {
            return new WorkflowAfterWriteHook() {
                @Override
                public void afterPersist() {
                    if (Boolean.TRUE.equals(FAIL.get())) {
                        throw new IllegalStateException("forced rollback");
                    }
                }
            };
        }
    }
}

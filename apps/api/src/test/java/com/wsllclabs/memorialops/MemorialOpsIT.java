package com.wsllclabs.memorialops;

import static org.assertj.core.api.Assertions.assertThat;

import com.wsllclabs.memorialops.identity.dto.CsrfResponse;
import com.wsllclabs.memorialops.identity.dto.MeResponse;
import com.wsllclabs.memorialops.shared.health.HealthSummary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@Testcontainers
@ActiveProfiles("test")
class MemorialOpsIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    void healthLoginAndSpaFallback() {
        ResponseEntity<HealthSummary> health = testRestTemplate.getForEntity("/api/health", HealthSummary.class);
        assertThat(health.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(health.getHeaders().getFirst("X-Correlation-ID")).isNotBlank();
        assertThat(health.getBody()).isNotNull();
        assertThat(health.getBody().status()).isEqualTo("UP");

        ResponseEntity<String> liveness = testRestTemplate.getForEntity("/actuator/health/liveness", String.class);
        assertThat(liveness.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<CsrfResponse> csrf = testRestTemplate.getForEntity("/api/auth/csrf", CsrfResponse.class);
        assertThat(csrf.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(csrf.getBody()).isNotNull();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-XSRF-TOKEN", csrf.getBody().token());
        String setCookie = csrf.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        if (setCookie != null) {
            headers.add(HttpHeaders.COOKIE, setCookie);
        }

        ResponseEntity<MeResponse> login = testRestTemplate.postForEntity(
                "/api/auth/login",
                new HttpEntity<>("{\"username\":\"coordinator\",\"password\":\"Coordinator!2026\"}", headers),
                MeResponse.class);
        assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(login.getBody()).isNotNull();
        assertThat(login.getBody().username()).isEqualTo("coordinator");

        ResponseEntity<String> denied = testRestTemplate.postForEntity(
                "/api/auth/login",
                new HttpEntity<>("{\"username\":\"coordinator\",\"password\":\"wrong\"}", headers),
                String.class);
        assertThat(denied.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        ResponseEntity<String> spa =
                testRestTemplate.exchange("/applications/demo-case", HttpMethod.GET, HttpEntity.EMPTY, String.class);
        assertThat(spa.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}

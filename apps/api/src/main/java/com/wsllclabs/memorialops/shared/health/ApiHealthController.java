package com.wsllclabs.memorialops.shared.health;

import com.wsllclabs.memorialops.config.AppProperties;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class ApiHealthController {

    private final DataSource dataSource;
    private final AppProperties appProperties;

    public ApiHealthController(DataSource dataSource, AppProperties appProperties) {
        this.dataSource = dataSource;
        this.appProperties = appProperties;
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    public HealthSummary health() {
        Map<String, String> components = new LinkedHashMap<>();
        components.put("db", probeDatabase());
        components.put("storage", configured(appProperties.integrations().storage().endpoint()));
        components.put("registry", configured(appProperties.integrations().registry().baseUrl()));
        components.put("calendar", configured(appProperties.integrations().calendar().baseUrl()));
        components.put("notification", configured(appProperties.integrations().notification().baseUrl()));
        components.put("weather", configured(appProperties.integrations().weather().baseUrl()));
        components.put("geocoding", configured(appProperties.integrations().geocoding().baseUrl()));
        components.put("partnerFeed", configured(appProperties.integrations().partnerFeed().host()));
        boolean down = components.values().stream().anyMatch("DOWN"::equals);
        return new HealthSummary(down ? "DOWN" : "UP", components);
    }

    private String probeDatabase() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2) ? "UP" : "DOWN";
        } catch (Exception exception) {
            return "DOWN";
        }
    }

    private static String configured(String value) {
        return value == null || value.isBlank() ? "UNKNOWN" : "CONFIGURED";
    }
}

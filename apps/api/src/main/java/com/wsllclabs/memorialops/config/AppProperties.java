package com.wsllclabs.memorialops.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Integrations integrations) {

    public record Integrations(
            AdapterProperties registry,
            CalendarProperties calendar,
            NotificationProperties notification,
            AdapterProperties weather,
            AdapterProperties geocoding,
            PartnerFeedProperties partnerFeed,
            StorageProperties storage) {}

    public record AdapterProperties(
            String baseUrl,
            @DefaultValue("2s") Duration connectTimeout,
            @DefaultValue("3s") Duration readTimeout,
            @DefaultValue("") String apiKey) {}

    public record CalendarProperties(
            String baseUrl,
            String clientId,
            String clientSecret,
            @DefaultValue("2s") Duration connectTimeout,
            @DefaultValue("5s") Duration readTimeout) {}

    public record NotificationProperties(
            String baseUrl,
            String hmacSecret,
            @DefaultValue("2s") Duration connectTimeout,
            @DefaultValue("5s") Duration readTimeout) {}

    public record PartnerFeedProperties(
            String host, @DefaultValue("22") int port, String username, String password) {}

    public record StorageProperties(
            String endpoint,
            String accessKey,
            String secretKey,
            @DefaultValue("attachments") String attachmentsBucket,
            @DefaultValue("permits") String permitsBucket,
            @DefaultValue("true") boolean pathStyle) {}
}

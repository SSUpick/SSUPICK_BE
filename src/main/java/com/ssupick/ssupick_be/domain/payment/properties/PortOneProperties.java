package com.ssupick.ssupick_be.domain.payment.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "portone")
public record PortOneProperties(
        String baseUrl,
        String apiSecret
) {
    public String resolvedBaseUrl() {
        return baseUrl == null || baseUrl.isBlank()
                ? "https://api.portone.io"
                : baseUrl;
    }
}

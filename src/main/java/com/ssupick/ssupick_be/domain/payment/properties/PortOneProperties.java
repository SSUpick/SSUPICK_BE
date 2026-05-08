package com.ssupick.ssupick_be.domain.payment.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "portone")
public record PortOneProperties(
        String baseUrl,
        String apiSecret,
        String storeId,
        String channelKey
) {
    // 설정된 baseUrl이 없으면 PortOne 운영 API URL을 기본값으로 반환합니다.
    public String resolvedBaseUrl() {
        return baseUrl == null || baseUrl.isBlank()
                ? "https://api.portone.io"
                : baseUrl;
    }
}

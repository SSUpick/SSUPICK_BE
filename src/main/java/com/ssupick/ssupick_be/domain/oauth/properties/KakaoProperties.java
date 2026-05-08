package com.ssupick.ssupick_be.domain.oauth.properties;

import com.ssupick.ssupick_be.domain.oauth.enums.RedirectType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "kakao")
public record KakaoProperties(
        String clientId,
        String redirectUri,
        String localRedirectUri,
        String prodRedirectUri,
        String clientSecret,
        String adminKey
) {
    // 프론트 실행 환경에 맞는 카카오 redirect_uri를 반환합니다.
    public String resolvedRedirectUri(RedirectType redirectType) {
        String selectedRedirectUri = switch (redirectType) {
            case LOCAL -> StringUtils.hasText(localRedirectUri) ? localRedirectUri : redirectUri;
            case PROD -> StringUtils.hasText(prodRedirectUri) ? prodRedirectUri : redirectUri;
        };

        if (!StringUtils.hasText(selectedRedirectUri)) {
            throw new IllegalStateException("Kakao redirect URI is not configured.");
        }
        return selectedRedirectUri;
    }

    // clientSecret, adminKey는 민감 정보이므로 마스킹합니다.
    @Override
    public String toString() {
        return "KakaoProperties{clientId=" + clientId
                + ", redirectUri=" + redirectUri
                + ", localRedirectUri=" + localRedirectUri
                + ", prodRedirectUri=" + prodRedirectUri
                + ", clientSecret=***"
                + ", adminKey=***}";
    }
}

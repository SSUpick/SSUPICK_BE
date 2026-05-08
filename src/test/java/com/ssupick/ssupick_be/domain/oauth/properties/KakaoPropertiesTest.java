package com.ssupick.ssupick_be.domain.oauth.properties;

import com.ssupick.ssupick_be.domain.oauth.enums.RedirectType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KakaoPropertiesTest {

    @Test
    void resolvedRedirectUri_returnsLocalRedirectUriWhenRedirectTypeIsLocal() {
        KakaoProperties properties = new KakaoProperties(
                "client-id",
                "fallback-uri",
                "local-uri",
                "prod-uri",
                "client-secret",
                "admin-key"
        );

        assertThat(properties.resolvedRedirectUri(RedirectType.LOCAL)).isEqualTo("local-uri");
    }

    @Test
    void resolvedRedirectUri_returnsProdRedirectUriWhenRedirectTypeIsProd() {
        KakaoProperties properties = new KakaoProperties(
                "client-id",
                "fallback-uri",
                "local-uri",
                "prod-uri",
                "client-secret",
                "admin-key"
        );

        assertThat(properties.resolvedRedirectUri(RedirectType.PROD)).isEqualTo("prod-uri");
    }

    @Test
    void resolvedRedirectUri_usesLegacyRedirectUriAsFallback() {
        KakaoProperties properties = new KakaoProperties(
                "client-id",
                "fallback-uri",
                null,
                null,
                "client-secret",
                "admin-key"
        );

        assertThat(properties.resolvedRedirectUri(RedirectType.LOCAL)).isEqualTo("fallback-uri");
        assertThat(properties.resolvedRedirectUri(RedirectType.PROD)).isEqualTo("fallback-uri");
    }

    @Test
    void resolvedRedirectUri_throwsWhenRedirectUriIsMissing() {
        KakaoProperties properties = new KakaoProperties(
                "client-id",
                null,
                null,
                null,
                "client-secret",
                "admin-key"
        );

        assertThatThrownBy(() -> properties.resolvedRedirectUri(RedirectType.LOCAL))
                .isInstanceOf(IllegalStateException.class);
    }
}

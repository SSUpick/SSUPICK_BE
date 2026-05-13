package com.ssupick.ssupick_be.domain.aiimage.properties;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class XaiPropertiesTest {

    @Test
    void resolvedPrompt_returnsXaiPromptWhenConfigured() {
        XaiProperties properties = new XaiProperties(
                "api-key", null, null, "grok prompt"
        );

        assertThat(properties.resolvedPrompt("gemini prompt")).isEqualTo("grok prompt");
    }

    @Test
    void resolvedPrompt_returnsFallbackPromptWhenXaiPromptIsBlank() {
        XaiProperties properties = new XaiProperties(
                "api-key", null, null, " "
        );

        assertThat(properties.resolvedPrompt("gemini prompt")).isEqualTo("gemini prompt");
    }
}

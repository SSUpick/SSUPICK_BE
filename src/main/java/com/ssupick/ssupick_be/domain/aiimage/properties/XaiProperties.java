package com.ssupick.ssupick_be.domain.aiimage.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "xai")
public record XaiProperties(
        String apiKey,
        String baseUrl,
        String imageModel,
        String prompt
) {
    private static final String DEFAULT_BASE_URL = "https://api.x.ai";
    private static final String DEFAULT_IMAGE_MODEL = "grok-imagine-image-quality";

    public String resolvedBaseUrl() {
        return baseUrl == null || baseUrl.isBlank()
                ? DEFAULT_BASE_URL
                : baseUrl;
    }

    public String resolvedImageModel() {
        return imageModel == null || imageModel.isBlank()
                ? DEFAULT_IMAGE_MODEL
                : imageModel;
    }

    public boolean enabled() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String resolvedPrompt(String fallbackPrompt) {
        return prompt == null || prompt.isBlank()
                ? fallbackPrompt
                : prompt;
    }
}

package com.ssupick.ssupick_be.domain.aiimage.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Objects;

@ConfigurationProperties(prefix = "gemini")
public record GeminiProperties(
        String apiKey,
        List<String> models,
        String prompt
) {
    private static final List<String> DEFAULT_MODELS = List.of(
        "gemini-3.1-flash-image-preview",
        "gemini-3-pro-image-preview",
        "gemini-2.5-flash-image"
    );

    public List<String> resolvedModels() {
        if (models == null || models.isEmpty()) {
            return DEFAULT_MODELS;
        }

        List<String> resolvedModels = models.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(model -> !model.isBlank())
                .distinct()
                .toList();

        return resolvedModels.isEmpty()
                ? DEFAULT_MODELS
                : resolvedModels;
    }
}

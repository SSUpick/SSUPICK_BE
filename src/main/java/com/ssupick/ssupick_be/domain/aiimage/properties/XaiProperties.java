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
    private static final String DEFAULT_PROMPT = "장난감처럼 친근하고 완성도 높은 3D 캐릭터를 만든다. "
            + "얼굴, 헤어스타일, 핵심 기능을 살리면서 업로드된 사진을 스타일화된 3D 캐릭터로 변형하세요. "
            + "은은한 질감과 부드러운 쉐이딩, 파스텔 톤, 깨끗한 배경의 부드러운 스튜디오 조명을 사용하세요.";

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

    public String resolvedPrompt() {
        return prompt == null || prompt.isBlank()
                ? DEFAULT_PROMPT
                : prompt;
    }
}

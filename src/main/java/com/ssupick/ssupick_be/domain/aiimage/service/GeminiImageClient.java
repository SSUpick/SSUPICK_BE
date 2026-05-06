package com.ssupick.ssupick_be.domain.aiimage.service;

import com.ssupick.ssupick_be.common.exception.GeneralException;
import com.ssupick.ssupick_be.common.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Gemini 이미지 생성 API 호출 전담 클라이언트
 *
 * 호출 흐름:
 * 1. 원본 이미지(byte[])를 Base64로 인코딩
 * 2. 텍스트 프롬프트 + 이미지를 Gemini API에 전송
 * 3. 응답에서 생성된 이미지 데이터(Base64)를 추출 → byte[] 반환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiImageClient {

    private final WebClient webClient;

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.base-url}")
    private String baseUrl;

    @Value("${gemini.model}")
    private String model;

    // 동물의 숲 스타일 이미지 생성 프롬프트
    private static final String ACNH_PROMPT =
            "Transform this person into an Animal Crossing: New Horizons style 3D character. " +
            "Maintain the person's key facial features, hair color, and overall vibe. " +
            "Use the soft, rounded, pastel-toned aesthetic of Animal Crossing. " +
            "The character should have a friendly expression and be shown from the shoulders up " +
            "on a clean, simple background. High quality, game-accurate style.";

    /**
     * 원본 이미지 byte[]를 받아 Gemini로 생성 이미지 byte[] 반환
     *
     * @param originalImageBytes 원본 사진 바이트 배열
     * @param mimeType           원본 이미지 MIME 타입 (ex. "image/jpeg")
     * @return Gemini가 생성한 이미지 byte[]
     */
    public byte[] generateAnimalCrossingImage(byte[] originalImageBytes, String mimeType) {
        String encodedImage = Base64.getEncoder().encodeToString(originalImageBytes);

        // Gemini API 요청 바디 구성
        // 참고: https://ai.google.dev/api/generate-content
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", ACNH_PROMPT),
                                Map.of("inline_data", Map.of(
                                        "mime_type", mimeType,
                                        "data", encodedImage
                                ))
                        ))
                ),
                "generationConfig", Map.of(
                        "response_modalities", List.of("IMAGE", "TEXT")
                )
        );

        String url = baseUrl + "/v1beta/models/" + model + ":generateContent?key=" + apiKey;

        log.info("[Gemini] 이미지 생성 요청 시작 - model: {}", model);

        // WebClient 동기 호출 (.block())
        Map<?, ?> response = webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .map(body -> {
                                    log.error("[Gemini] API 오류 응답: {}", body);
                                    return new GeneralException(ErrorStatus.AI_IMAGE_GENERATION_FAILED);
                                }))
                .bodyToMono(Map.class)
                .block();

        return extractImageBytes(response);
    }

    /**
     * Gemini 응답에서 이미지 Base64 데이터를 추출해 byte[]로 반환
     * 응답 구조: response.candidates[0].content.parts[*].inline_data.data
     */
    @SuppressWarnings("unchecked")
    private byte[] extractImageBytes(Map<?, ?> response) {
        try {
            List<?> candidates = (List<?>) response.get("candidates");
            Map<?, ?> content = (Map<?, ?>) ((Map<?, ?>) candidates.get(0)).get("content");
            List<?> parts = (List<?>) content.get("parts");

            for (Object part : parts) {
                Map<?, ?> partMap = (Map<?, ?>) part;
                if (partMap.containsKey("inline_data")) {
                    Map<?, ?> inlineData = (Map<?, ?>) partMap.get("inline_data");
                    String base64Data = (String) inlineData.get("data");
                    log.info("[Gemini] 이미지 생성 성공 - 응답 데이터 추출 완료");
                    return Base64.getDecoder().decode(base64Data);
                }
            }
        } catch (Exception e) {
            log.error("[Gemini] 응답 파싱 실패", e);
        }
        throw new GeneralException(ErrorStatus.AI_IMAGE_GENERATION_FAILED);
    }
}

package com.ssupick.ssupick_be.domain.aiimage.service;

import com.google.common.collect.ImmutableList;
import com.google.genai.Client;
import com.google.genai.ResponseStream;
import com.google.genai.errors.ApiException;
import com.google.genai.types.*;
import com.ssupick.ssupick_be.common.exception.GeneralException;
import com.ssupick.ssupick_be.common.status.ErrorStatus;
import com.ssupick.ssupick_be.domain.aiimage.properties.GeminiProperties;
import com.ssupick.ssupick_be.domain.aiimage.properties.XaiProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Google GenAI SDK 기반 Gemini 이미지 생성 클라이언트
 * SDK 스트리밍 방식으로 호출 — WebClient REST 방식 대비 안정적
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiImageClient {

    private final GeminiProperties geminiProperties;
    private final XaiProperties xaiProperties;
    private final GrokImageClient grokImageClient;
    private Client client;

    @PostConstruct
    void init() {
        this.client = Client.builder().apiKey(geminiProperties.apiKey()).build();
    }

    /**
     * 원본 이미지 byte[] → Gemini 스트리밍 호출 → 생성 이미지 byte[] 반환
     */
    public byte[] generateAnimalCrossingImage(byte[] originalImageBytes, String mimeType) {
        List<String> fallbackModels = geminiProperties.resolvedModels();
        Exception lastException = null;

        for (String fallbackModel : fallbackModels) {
            try {
                log.info("[Gemini] 이미지 생성 요청 시작 - model: {}", fallbackModel);
                byte[] generatedImageBytes = generateWithModel(
                        client, fallbackModel, originalImageBytes, mimeType
                );

                if (!Objects.equals(fallbackModel, fallbackModels.get(0))) {
                    log.warn("[Gemini] 예비 모델로 폴백되었습니다 - model: {}", fallbackModel);
                }
                return generatedImageBytes;
            } catch (GeneratedImageMissingException e) {
                log.error("[Gemini] 응답에 생성 이미지가 없어 폴백하지 않습니다 - model: {}", fallbackModel, e);
                throw new GeneralException(ErrorStatus.AI_IMAGE_GENERATION_FAILED, e);
            } catch (ApiException e) {
                if (!isFallbackApiError(e)) {
                    log.error("[Gemini] 비일시적 API 오류로 폴백하지 않습니다 - model: {}, code: {}",
                            fallbackModel, e.code(), e);
                    throw new GeneralException(ErrorStatus.AI_IMAGE_GENERATION_FAILED, e);
                }

                lastException = e;
                log.warn("[Gemini] 일시적 API 오류, 다음 모델을 시도합니다 - model: {}, code: {}",
                        fallbackModel, e.code(), e);
            } catch (Exception e) {
                lastException = e;
                log.warn("[Gemini] 모델 호출 실패, 다음 모델을 시도합니다 - model: {}", fallbackModel, e);
            }
        }

        if (grokImageClient.isEnabled()) {
            log.warn("[Gemini] 모든 Gemini 모델 실패, Grok 이미지 편집으로 폴백합니다.");
            return grokImageClient.editImage(
                    originalImageBytes,
                    mimeType,
                    xaiProperties.resolvedPrompt(geminiProperties.prompt())
            );
        }

        throw new GeneralException(ErrorStatus.AI_IMAGE_GENERATION_FAILED, lastException);
    }

    private byte[] generateWithModel(
            Client client,
            String targetModel,
            byte[] originalImageBytes,
            String mimeType
    ) {
        // 이미지 Part 구성 — Blob.builder()로 mimeType + data 주입
        Part imagePart = Part.builder()
                .inlineData(Blob.builder()
                        .mimeType(mimeType)
                        .data(originalImageBytes)
                        .build())
                .build();

        // 텍스트 프롬프트 Part
        Part textPart = Part.builder()
                .text(geminiProperties.prompt())
                .build();

        // Content 구성
        List<Content> contents = ImmutableList.of(
                Content.builder()
                        .role("user")
                        .parts(ImmutableList.of(textPart, imagePart))
                        .build()
        );

        // 생성 설정 — 이미지 + 텍스트 응답 모달리티
        GenerateContentConfig config = GenerateContentConfig.builder()
                .responseModalities(ImmutableList.of("IMAGE", "TEXT"))
                .build();

        // 스트리밍 응답에서 이미지 데이터 추출
        try (ResponseStream<GenerateContentResponse> responseStream =
                     client.models.generateContentStream(targetModel, contents, config)) {

            for (GenerateContentResponse res : responseStream) {
                // 편의 메서드 parts() 사용 — null safe
                ImmutableList<Part> parts = res.parts();
                if (parts == null) continue;

                for (Part part : parts) {
                    if (part.inlineData().isPresent()) {
                        var inlineDataOpt = part.inlineData();
                        if (inlineDataOpt.isEmpty())
                            continue;
                        var imageBytesOpt = inlineDataOpt.get().data();
                        if (imageBytesOpt.isEmpty() || imageBytesOpt.get().length == 0)
                            continue;
                        byte[] imageBytes = imageBytesOpt.get();
                        log.info("[Gemini] 이미지 생성 성공 - model: {}", targetModel);
                        return imageBytes;
                    }
                }
            }
        }

        throw new GeneratedImageMissingException();
    }

    private boolean isFallbackApiError(ApiException exception) {
        int statusCode = exception.code();
        return statusCode == 429
                || statusCode == 500
                || statusCode == 502
                || statusCode == 503
                || statusCode == 504
                || statusCode == 404
                || isModelUnsupportedError(exception);
    }

    private boolean isModelUnsupportedError(ApiException exception) {
        if (exception.code() != 400) {
            return false;
        }

        String message = exception.message();
        if (message == null) {
            return false;
        }

        String normalizedMessage = message.toLowerCase();
        return normalizedMessage.contains("model")
                && (normalizedMessage.contains("not found")
                || normalizedMessage.contains("not supported")
                || normalizedMessage.contains("not available")
                || normalizedMessage.contains("does not support"));
    }

    private static class GeneratedImageMissingException extends RuntimeException {
        private GeneratedImageMissingException() {
            super("Gemini response did not contain generated image data.");
        }
    }
}

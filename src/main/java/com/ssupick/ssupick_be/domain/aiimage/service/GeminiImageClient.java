package com.ssupick.ssupick_be.domain.aiimage.service;

import com.google.common.collect.ImmutableList;
import com.google.genai.Client;
import com.google.genai.ResponseStream;
import com.google.genai.types.*;
import com.ssupick.ssupick_be.common.exception.GeneralException;
import com.ssupick.ssupick_be.common.status.ErrorStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Google GenAI SDK 기반 Gemini 이미지 생성 클라이언트
 * SDK 스트리밍 방식으로 호출 — WebClient REST 방식 대비 안정적
 */
@Slf4j
@Component
public class GeminiImageClient {

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String model;

    @Value("${gemini.prompt}")
    private String prompt;

    /**
     * 원본 이미지 byte[] → Gemini 스트리밍 호출 → 생성 이미지 byte[] 반환
     */
    public byte[] generateAnimalCrossingImage(byte[] originalImageBytes, String mimeType) {
        log.info("[Gemini] 이미지 생성 요청 시작 - model: {}", model);

        Client client = Client.builder().apiKey(apiKey).build();

        // 이미지 Part 구성 — Blob.builder()로 mimeType + data 주입
        Part imagePart = Part.builder()
                .inlineData(Blob.builder()
                        .mimeType(mimeType)
                        .data(originalImageBytes)
                        .build())
                .build();

        // 텍스트 프롬프트 Part
        Part textPart = Part.builder()
                .text(prompt)
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
                     client.models.generateContentStream(model, contents, config)) {

            for (GenerateContentResponse res : responseStream) {
                // 편의 메서드 parts() 사용 — null safe
                ImmutableList<Part> parts = res.parts();
                if (parts == null) continue;

                for (Part part : parts) {
                    if (part.inlineData().isPresent()) {
                        byte[] imageBytes = part.inlineData().get().data().get();
                        log.info("[Gemini] 이미지 생성 성공");
                        return imageBytes;
                    }
                }
            }

        } catch (Exception e) {
            log.error("[Gemini] 이미지 생성 실패", e);
            throw new GeneralException(ErrorStatus.AI_IMAGE_GENERATION_FAILED, e);
        }

        throw new GeneralException(ErrorStatus.AI_IMAGE_GENERATION_FAILED);
    }
}

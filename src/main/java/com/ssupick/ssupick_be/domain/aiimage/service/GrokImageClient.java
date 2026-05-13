package com.ssupick.ssupick_be.domain.aiimage.service;

import com.ssupick.ssupick_be.common.exception.GeneralException;
import com.ssupick.ssupick_be.common.status.ErrorStatus;
import com.ssupick.ssupick_be.domain.aiimage.properties.XaiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Base64;
import java.util.List;

@Slf4j
@Component
public class GrokImageClient {

    private final XaiProperties xaiProperties;
    private final WebClient webClient;

    public GrokImageClient(
            XaiProperties xaiProperties,
            @Qualifier("webClient") WebClient webClient
    ) {
        this.xaiProperties = xaiProperties;
        this.webClient = webClient;
    }

    public boolean isEnabled() {
        return xaiProperties.enabled();
    }

    public byte[] editImage(byte[] originalImageBytes, String mimeType, String prompt) {
        if (!xaiProperties.enabled()) {
            throw new IllegalStateException("xAI API key is not configured.");
        }

        String model = xaiProperties.resolvedImageModel();
        String dataUri = toDataUri(originalImageBytes, mimeType);
        XaiImageEditRequest request = new XaiImageEditRequest(
                model,
                prompt,
                new XaiImageReference("image_url", dataUri)
        );

        try {
            log.info("[Grok] 이미지 편집 요청 시작 - model: {}", model);
            XaiImageResponse response = webClient.post()
                    .uri(xaiProperties.resolvedBaseUrl() + "/v1/images/edits")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + xaiProperties.apiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(XaiImageResponse.class)
                    .block();

            XaiImageData imageData = firstImageData(response);
            if (imageData.b64Json() != null && !imageData.b64Json().isBlank()) {
                log.info("[Grok] 이미지 편집 성공 - model: {}, response: b64_json", model);
                return Base64.getDecoder().decode(imageData.b64Json());
            }

            if (imageData.url() != null && !imageData.url().isBlank()) {
                log.info("[Grok] 이미지 편집 성공 - model: {}, response: url", model);
                return downloadImage(imageData.url());
            }

            throw new GeneratedImageMissingException();
        } catch (WebClientResponseException e) {
            log.error("[Grok] API 오류 - model: {}, status: {}, body: {}",
                    model, e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new GeneralException(ErrorStatus.AI_IMAGE_GENERATION_FAILED, e);
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Grok] 이미지 편집 실패 - model: {}", model, e);
            throw new GeneralException(ErrorStatus.AI_IMAGE_GENERATION_FAILED, e);
        }
    }

    private byte[] downloadImage(String imageUrl) {
        byte[] imageBytes = webClient.get()
                .uri(imageUrl)
                .retrieve()
                .bodyToMono(byte[].class)
                .block();

        if (imageBytes == null || imageBytes.length == 0) {
            throw new GeneratedImageMissingException();
        }
        return imageBytes;
    }

    private XaiImageData firstImageData(XaiImageResponse response) {
        if (response == null || response.data() == null || response.data().isEmpty()) {
            throw new GeneratedImageMissingException();
        }
        return response.data().get(0);
    }

    private String toDataUri(byte[] imageBytes, String mimeType) {
        String resolvedMimeType = mimeType == null || mimeType.isBlank()
                ? MediaType.IMAGE_JPEG_VALUE
                : mimeType;
        return "data:" + resolvedMimeType + ";base64," + Base64.getEncoder().encodeToString(imageBytes);
    }

    private record XaiImageEditRequest(
            String model,
            String prompt,
            XaiImageReference image
    ) {}

    private record XaiImageReference(
            String type,
            String url
    ) {}

    private record XaiImageResponse(
            List<XaiImageData> data
    ) {}

    private record XaiImageData(
            String url,
            @com.fasterxml.jackson.annotation.JsonProperty("b64_json")
            String b64Json
    ) {}

    private static class GeneratedImageMissingException extends RuntimeException {
        private GeneratedImageMissingException() {
            super("Grok response did not contain generated image data.");
        }
    }
}

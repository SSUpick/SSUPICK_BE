package com.ssupick.ssupick_be.domain.aiimage.service;

import com.ssupick.ssupick_be.common.exception.GeneralException;
import com.ssupick.ssupick_be.common.s3.S3Uploader;
import com.ssupick.ssupick_be.common.status.ErrorStatus;
import com.ssupick.ssupick_be.domain.aiimage.entity.AiImage;
import com.ssupick.ssupick_be.domain.aiimage.properties.XaiProperties;
import com.ssupick.ssupick_be.domain.aiimage.repository.AiImageRepository;
import com.ssupick.ssupick_be.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrokAsyncProcessor {

    private final AiImageRepository aiImageRepository;
    private final UserRepository userRepository;
    private final GrokImageClient grokImageClient;
    private final XaiProperties xaiProperties;
    private final S3Uploader s3Uploader;

    @Async("aiImageExecutor")
    @Transactional
    public void process(Long aiImageId, Long userId,
                        byte[] imageBytes, String mimeType, String extension) {

        try {
            log.info("[Grok] 비동기 호출 시작 - aiImageId: {}", aiImageId);

            byte[] generatedImageBytes = grokImageClient.editImage(
                    imageBytes,
                    mimeType,
                    xaiProperties.resolvedPrompt()
            );

            String generatedKey = s3Uploader.uploadBytes(generatedImageBytes, "generated/" + userId, extension);
            log.info("[Grok] 생성 완료 - aiImageId: {}, key: {}", aiImageId, generatedKey);

            AiImage aiImage = aiImageRepository.findById(aiImageId)
                    .orElseThrow(() -> new GeneralException(ErrorStatus.AI_IMAGE_NOT_FOUND));
            aiImage.markDone(generatedKey);

        } catch (Exception e) {
            log.error("[Grok] 생성 실패 - aiImageId: {}", aiImageId, e);

            aiImageRepository.findById(aiImageId).ifPresent(AiImage::markFailed);

            userRepository.findById(userId).ifPresent(user -> {
                user.restoreGenerationCount();
                log.info("[Grok] 횟수 복구 - userId: {}", userId);
            });
        }
    }
}

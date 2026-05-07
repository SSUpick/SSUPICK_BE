package com.ssupick.ssupick_be.domain.aiimage.service;

import com.ssupick.ssupick_be.common.exception.GeneralException;
import com.ssupick.ssupick_be.common.s3.S3Uploader;
import com.ssupick.ssupick_be.common.status.ErrorStatus;
import com.ssupick.ssupick_be.domain.aiimage.entity.AiImage;
import com.ssupick.ssupick_be.domain.aiimage.repository.AiImageRepository;
import com.ssupick.ssupick_be.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gemini 비동기 처리 전담 컴포넌트
 *
 * AiImageService 내부에서 @Async + @Transactional을 같이 쓰면
 * 자기 호출(self-invocation) 문제로 프록시를 거치지 않아 둘 다 동작하지 않음.
 * 별도 빈으로 분리해서 Spring 프록시가 정상 적용되도록 해결.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GeminiAsyncProcessor {

    private final AiImageRepository aiImageRepository;
    private final UserRepository userRepository;
    private final GeminiImageClient geminiImageClient;
    private final S3Uploader s3Uploader;

    /**
     * 백그라운드에서 Gemini 호출 + 결과 저장
     *
     * @Async  — 별도 스레드에서 실행 (메인 요청 즉시 반환)
     * @Transactional — 독립 트랜잭션 (generateImage 트랜잭션과 분리)
     */
    @Async("aiImageExecutor")
    @Transactional
    public void process(Long aiImageId, Long userId,
                        byte[] imageBytes, String mimeType, String extension) {

        try {
            log.info("[Gemini] 비동기 호출 시작 - aiImageId: {}", aiImageId);

            byte[] generatedImageBytes = geminiImageClient.generateAnimalCrossingImage(imageBytes, mimeType);

            // 생성 이미지 S3 업로드 — S3 key 저장 (Presigned URL 아님)
            String generatedKey = s3Uploader.uploadBytes(generatedImageBytes, "generated/" + userId, extension);
            log.info("[Gemini] 생성 완료 - aiImageId: {}, key: {}", aiImageId, generatedKey);

            // generateImage() 트랜잭션 커밋 완료 후 조회 — 재시도 없이 안전하게 처리
            AiImage aiImage = aiImageRepository.findById(aiImageId)
                    .orElseThrow(() -> new GeneralException(ErrorStatus.AI_IMAGE_NOT_FOUND));
            aiImage.markDone(generatedKey);

        } catch (GeneralException e) {
            throw e; // AI_IMAGE_NOT_FOUND 등 비즈니스 예외는 그대로 전파
        } catch (Exception e) {
            log.error("[Gemini] 생성 실패 - aiImageId: {}", aiImageId, e);

            aiImageRepository.findById(aiImageId).ifPresent(AiImage::markFailed);

            // 생성 실패 시 차감된 횟수 복구
            userRepository.findByIdAndDeletedFalse(userId).ifPresent(user -> {
                user.restoreGenerationCount();
                log.info("[Gemini] 횟수 복구 - userId: {}", userId);
            });
        }
    }
}

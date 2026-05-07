package com.ssupick.ssupick_be.domain.aiimage.service;

import com.ssupick.ssupick_be.common.exception.GeneralException;
import com.ssupick.ssupick_be.common.s3.S3Uploader;
import com.ssupick.ssupick_be.common.status.ErrorStatus;
import com.ssupick.ssupick_be.domain.aiimage.dto.response.AiImageResponse;
import com.ssupick.ssupick_be.domain.aiimage.entity.AiImage;
import com.ssupick.ssupick_be.domain.aiimage.entity.AiImageStatus;
import com.ssupick.ssupick_be.domain.aiimage.repository.AiImageRepository;
import com.ssupick.ssupick_be.domain.user.entity.User;
import com.ssupick.ssupick_be.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiImageService {

    private final UserRepository userRepository;
    private final AiImageRepository aiImageRepository;
    private final GeminiAsyncProcessor geminiAsyncProcessor;
    private final S3Uploader s3Uploader;

    /**
     * [비동기] AI 이미지 생성 요청
     * 1. 유저 조회 + 잔여 횟수 확인
     * 2. 원본 이미지 S3 업로드
     * 3. AiImage 엔티티 PENDING 상태로 저장 후 즉시 응답 반환
     * 4. 백그라운드에서 Gemini 호출 + 결과 저장 (별도 트랜잭션)
     */
    @Transactional
    public AiImageResponse generateImage(Long userId, MultipartFile originalImageFile) {

        // 1. 유저 조회 + 잔여 횟수 확인 + 이미 AI 이미지 생성했는지 확인
        User user = getActiveUserOrThrow(userId);
        if (user.getRemainingGenerationCount() <= 0) {
            throw new GeneralException(ErrorStatus.AI_IMAGE_GENERATION_LIMIT_EXCEEDED);
        }


        // 2. 원본 이미지 S3 업로드
        String originalKey = s3Uploader.upload(originalImageFile, "original/" + userId);
        log.info("[AiImage] 원본 이미지 업로드 완료 - userId: {}, key: {}", userId, originalKey);

        // 3. AiImage 엔티티 PENDING 상태로 저장
        AiImage aiImage = AiImage.builder()
                .user(user)
                .originalImageUrl(originalKey)
                .status(AiImageStatus.PENDING)
                .build();
        aiImageRepository.save(aiImage);

        // 횟수 선차감 — 백그라운드 실패 시 복구 로직은 processGeminiAsync에서 처리
        user.decreaseGenerationCount();

        // 4. 백그라운드에서 Gemini 호출 — 별도 빈(GeminiAsyncProcessor)으로 위임
        // self-invocation 방지: 같은 클래스 내 @Async 호출은 프록시를 거치지 않아 동작 안 함
        byte[] imageBytes = readFileBytes(originalImageFile);
        String mimeType = originalImageFile.getContentType() != null
                ? originalImageFile.getContentType() : "image/jpeg";
        String extension = extractExtension(originalImageFile.getOriginalFilename());

        geminiAsyncProcessor.process(aiImage.getId(), userId, imageBytes, mimeType, extension);

        // 5. PENDING 상태로 즉시 응답 반환
        String originalPresignedUrl = s3Uploader.generatePresignedUrl(originalKey);
        return AiImageResponse.of(aiImage, originalPresignedUrl, null,
                user.getRemainingGenerationCount());
    }

    /**
     * 단건 상태 조회 — 프론트가 폴링으로 PENDING → DONE 확인 시 사용
     */
    @Transactional(readOnly = true)
    public AiImageResponse getImageStatus(Long userId, Long aiImageId) {
        User user = getActiveUserOrThrow(userId);
        AiImage aiImage = aiImageRepository.findById(aiImageId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.AI_IMAGE_NOT_FOUND));

        if (!aiImage.getUser().getId().equals(userId)) {
            throw new GeneralException(ErrorStatus.AI_IMAGE_NOT_OWNED);
        }

        String originalPresignedUrl = s3Uploader.generatePresignedUrl(aiImage.getOriginalImageUrl());
        String generatedPresignedUrl = aiImage.getGeneratedImageUrl() != null
                ? s3Uploader.generatePresignedUrl(aiImage.getGeneratedImageUrl()) : null;

        return AiImageResponse.of(aiImage, originalPresignedUrl, generatedPresignedUrl,
                user.getRemainingGenerationCount());
    }

    /**
     * 유저의 전체 AI 이미지 목록 조회
     */
    @Transactional(readOnly = true)
    public List<AiImageResponse> getImageList(Long userId) {
        User user = getActiveUserOrThrow(userId);

        return aiImageRepository.findAllByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(aiImage -> AiImageResponse.of(
                        aiImage,
                        s3Uploader.generatePresignedUrl(aiImage.getOriginalImageUrl()),
                        aiImage.getGeneratedImageUrl() != null
                                ? s3Uploader.generatePresignedUrl(aiImage.getGeneratedImageUrl())
                                : null,
                        user.getRemainingGenerationCount()
                ))
                .toList();
    }

    /**
     * 최종 프로필 이미지 확정 — DONE 상태 이미지만 선택 가능
     */
    @Transactional
    public void selectProfileImage(Long userId, Long aiImageId) {
        User user = getActiveUserOrThrow(userId);

        AiImage target = aiImageRepository.findById(aiImageId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.AI_IMAGE_NOT_FOUND));

        if (!target.getUser().getId().equals(userId)) {
            throw new GeneralException(ErrorStatus.AI_IMAGE_NOT_OWNED);
        }

        // DONE 상태 아니면 선택 불가
        if (target.getStatus() != AiImageStatus.DONE || target.getGeneratedImageUrl() == null) {
            throw new GeneralException(ErrorStatus.AI_IMAGE_GENERATION_FAILED);
        }

        // 기존 선택 이미지 해제
        aiImageRepository.findByUserAndSelectedTrue(user)
                .ifPresent(AiImage::deselect);

        // 새 이미지 선택 + 프로필 URL 반영 — S3 key 저장 (Presigned URL은 만료되므로 저장 X)
        target.select();
        user.updateProfileUrl(target.getGeneratedImageUrl());

        log.info("[AiImage] 프로필 이미지 확정 - userId: {}, aiImageId: {}", userId, aiImageId);
    }

    // ── private 헬퍼 ──────────────────────────────────────────────────────────

    private User getActiveUserOrThrow(Long userId) {
        return userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
    }

    private byte[] readFileBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new GeneralException(ErrorStatus.AI_IMAGE_UPLOAD_FAILED, e);
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "jpg";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}

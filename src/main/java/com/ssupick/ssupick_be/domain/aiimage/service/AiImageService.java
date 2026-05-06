package com.ssupick.ssupick_be.domain.aiimage.service;

import com.ssupick.ssupick_be.common.exception.GeneralException;
import com.ssupick.ssupick_be.common.s3.S3Uploader;
import com.ssupick.ssupick_be.common.status.ErrorStatus;
import com.ssupick.ssupick_be.domain.aiimage.dto.response.AiImageResponse;
import com.ssupick.ssupick_be.domain.aiimage.entity.AiImage;
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
    private final GeminiImageClient geminiImageClient;
    private final S3Uploader s3Uploader;

    /**
     * AI 이미지 생성 전체 흐름
     *
     * 1. 유저 조회 + 잔여 횟수 확인
     * 2. 원본 이미지 S3 업로드
     * 3. AiImage 엔티티 저장 (generated URL은 아직 null)
     * 4. Gemini API 호출
     * 5. 생성 이미지 S3 업로드 + 엔티티 업데이트
     * 6. 잔여 횟수 차감
     * 7. Presigned URL 발급 후 응답 반환
     */
    @Transactional
    public AiImageResponse generateImage(Long userId, MultipartFile originalImageFile) {

        // 1. 유저 조회 + 잔여 횟수 확인
        User user = getActiveUserOrThrow(userId);
        if (user.getRemainingGenerationCount() <= 0) {
            throw new GeneralException(ErrorStatus.AI_IMAGE_GENERATION_LIMIT_EXCEEDED);
        }

        // 2. 원본 이미지 S3 업로드
        String originalKey = s3Uploader.upload(originalImageFile, "original");
        log.info("[AiImage] 원본 이미지 업로드 완료 - userId: {}, key: {}", userId, originalKey);

        // 3. AiImage 엔티티 저장 (generated URL은 Gemini 응답 후 채움)
        AiImage aiImage = AiImage.builder()
                .user(user)
                .originalImageUrl(originalKey)
                .build();
        aiImageRepository.save(aiImage);

        // 4. Gemini API 호출
        byte[] generatedImageBytes = callGeminiWithFile(originalImageFile);

        // 5. 생성 이미지 S3 업로드 + 엔티티 업데이트
        String extension = extractExtension(originalImageFile.getOriginalFilename());
        String generatedKey = s3Uploader.uploadBytes(generatedImageBytes, "generated", extension);
        aiImage.saveGeneratedImageUrl(generatedKey);
        log.info("[AiImage] 생성 이미지 업로드 완료 - userId: {}, key: {}", userId, generatedKey);

        // 6. 잔여 횟수 차감 (User 엔티티 메서드 — 0이하면 예외)
        user.decreaseGenerationCount();

        // 7. Presigned URL 발급 후 응답 반환
        String originalPresignedUrl = s3Uploader.generatePresignedUrl(originalKey);
        String generatedPresignedUrl = s3Uploader.generatePresignedUrl(generatedKey);

        return AiImageResponse.of(aiImage, originalPresignedUrl, generatedPresignedUrl,
                user.getRemainingGenerationCount());
    }

    /**
     * 유저의 전체 AI 이미지 목록 조회
     * 생성한 이미지들 + 각각의 Presigned URL 반환
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
     * 최종 프로필 이미지 확정
     *
     * 1. 기존에 선택된 이미지 있으면 deselect
     * 2. 선택한 이미지 select
     * 3. User.profileUrl 업데이트
     */
    @Transactional
    public void selectProfileImage(Long userId, Long aiImageId) {
        User user = getActiveUserOrThrow(userId);

        // 선택하려는 이미지 조회 + 소유권 확인
        AiImage target = aiImageRepository.findById(aiImageId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.AI_IMAGE_NOT_FOUND));

        if (!target.getUser().getId().equals(userId)) {
            throw new GeneralException(ErrorStatus.AI_IMAGE_NOT_OWNED);
        }

        if (target.getGeneratedImageUrl() == null) {
            throw new GeneralException(ErrorStatus.AI_IMAGE_GENERATION_FAILED);
        }

        // 기존 선택 이미지 해제
        aiImageRepository.findByUserAndSelectedTrue(user)
                .ifPresent(AiImage::deselect);

        // 새 이미지 선택 + 프로필 URL 반영
        target.select();
        String presignedUrl = s3Uploader.generatePresignedUrl(target.getGeneratedImageUrl());
        user.updateProfileUrl(presignedUrl);

        log.info("[AiImage] 프로필 이미지 확정 - userId: {}, aiImageId: {}", userId, aiImageId);
    }

    // ── private 헬퍼 ──────────────────────────────────────────────────────────

    private User getActiveUserOrThrow(Long userId) {
        return userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
    }

    private byte[] callGeminiWithFile(MultipartFile file) {
        try {
            String mimeType = file.getContentType() != null ? file.getContentType() : "image/jpeg";
            return geminiImageClient.generateAnimalCrossingImage(file.getBytes(), mimeType);
        } catch (IOException e) {
            throw new GeneralException(ErrorStatus.AI_IMAGE_UPLOAD_FAILED, e);
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "jpg";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}

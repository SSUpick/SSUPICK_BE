package com.ssupick.ssupick_be.common.s3;

import com.ssupick.ssupick_be.common.exception.GeneralException;
import com.ssupick.ssupick_be.common.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Uploader {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     * MultipartFile → S3 업로드 후 S3 key 반환
     * key 형식: {folder}/{UUID}.{확장자}
     * ex) original/{userId}/UUID.jpg
     */
    public String upload(MultipartFile file, String folder) {
        validateImageFile(file);

        String key = generateKey(folder, extractExtension(file.getOriginalFilename()));

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            log.info("[S3] 업로드 완료 - key: {}", key);
            return key;

        } catch (IOException e) {
            log.error("[S3] 업로드 실패 - key: {}", key, e);
            throw new GeneralException(ErrorStatus.AI_IMAGE_UPLOAD_FAILED, e);
        }
    }

    /**
     * byte[] 이미지 데이터 → S3 업로드 후 S3 key 반환
     * Gemini 응답 이미지(byte[]) 저장 시 사용
     */
    public String uploadBytes(byte[] imageBytes, String folder, String extension) {
        String key = generateKey(folder, extension);

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("image/" + extension)
                .contentLength((long) imageBytes.length)
                .build();

        s3Client.putObject(putRequest, RequestBody.fromBytes(imageBytes));
        log.info("[S3] byte[] 업로드 완료 - key: {}", key);
        return key;
    }

    /**
     * S3 key → Presigned URL 발급 (7일 만료)
     * 프론트에 이미지 URL 전달 시 사용
     */
    public String generatePresignedUrl(String key) {
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofDays(7))
                .getObjectRequest(GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build())
                .build();

        String url = s3Presigner.presignGetObject(presignRequest).url().toString();
        log.info("[S3] Presigned URL 발급 - key: {}, 만료: 7일", key);
        return url;
    }

    /**
     * S3 객체 삭제
     * 유저 탈퇴 또는 이미지 초기화 시 사용
     */
    public void delete(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
        log.info("[S3] 삭제 완료 - key: {}", key);
    }

    // ── private 헬퍼 ──────────────────────────────────────────────────────────

    /** 이미지 파일 유효성 검증 (형식, 크기) — 프로젝트 예외 체계 통일 */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new GeneralException(ErrorStatus.AI_IMAGE_UPLOAD_FAILED);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            log.warn("[S3] 유효하지 않은 파일 형식 - contentType: {}", contentType);
            throw new GeneralException(ErrorStatus.AI_IMAGE_UPLOAD_FAILED);
        }

        // 10MB 제한
        long maxSize = 10 * 1024 * 1024L;
        if (file.getSize() > maxSize) {
            log.warn("[S3] 파일 크기 초과 - size: {}MB", file.getSize() / (1024 * 1024));
            throw new GeneralException(ErrorStatus.AI_IMAGE_UPLOAD_FAILED);
        }
    }

    /** S3 key 생성: {folder}/{UUID}.{ext} */
    private String generateKey(String folder, String extension) {
        return folder + "/" + UUID.randomUUID() + "." + extension;
    }

    /** 파일명에서 확장자 추출 (없으면 "jpg" 기본값) */
    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}

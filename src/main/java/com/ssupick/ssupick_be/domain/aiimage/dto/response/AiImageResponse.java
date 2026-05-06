package com.ssupick.ssupick_be.domain.aiimage.dto.response;

import com.ssupick.ssupick_be.domain.aiimage.entity.AiImage;

public record AiImageResponse(

        Long aiImageId,                 // 이미지 식별자 (프로필 확정 시 사용)
        String originalImageUrl,        // 원본 사진 Presigned URL
        String generatedImageUrl,       // 생성된 이미지 Presigned URL
        boolean selected,               // 현재 프로필로 선택된 이미지 여부

        // 생성 횟수 정보
        int remainingCount,             // 남은 생성 횟수
        int totalCount                  // 총 생성 가능 횟수 (고정 3)
) {
    public static AiImageResponse of(AiImage aiImage, String originalPresignedUrl,
                                     String generatedPresignedUrl, int remainingCount) {
        return new AiImageResponse(
                aiImage.getId(),
                originalPresignedUrl,
                generatedPresignedUrl,
                aiImage.isSelected(),
                remainingCount,
                3
        );
    }
}

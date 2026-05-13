package com.ssupick.ssupick_be.domain.aiimage.entity;

public enum AiImageStatus {

    PENDING,   // AI 이미지 생성 중 (백그라운드 처리 중)
    DONE,      // 생성 완료
    FAILED     // 생성 실패
}

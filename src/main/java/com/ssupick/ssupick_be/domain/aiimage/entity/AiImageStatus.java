package com.ssupick.ssupick_be.domain.aiimage.entity;

public enum AiImageStatus {

    PENDING,   // Gemini 호출 중 (백그라운드 처리 중)
    DONE,      // 생성 완료
    FAILED     // 생성 실패
}

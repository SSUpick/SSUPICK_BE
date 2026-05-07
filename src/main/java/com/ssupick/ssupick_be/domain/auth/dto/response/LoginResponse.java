package com.ssupick.ssupick_be.domain.auth.dto.response;

import com.ssupick.ssupick_be.domain.user.entity.User;
import com.ssupick.ssupick_be.domain.user.enums.OnboardingStatus;

public record LoginResponse(
        Long userId,
        String accessToken,
        String refreshToken,
        boolean onboardingCompleted,   // 온보딩 완료 여부
        boolean aiImageGenerated,       // selected=true 이미지 존재 여부
        int remainingCouponCount // 남은 쿠폰 개수
) {
    public static LoginResponse of(User user, String accessToken, String refreshToken,
                                   boolean aiImageGenerated) {
        return new LoginResponse(
                user.getId(),
                accessToken,
                refreshToken,
                user.getOnboardingStatus() == OnboardingStatus.COMPLETED,
                aiImageGenerated,
                user.getRemainingCouponCount()
        );
    }
}

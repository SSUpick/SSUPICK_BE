package com.ssupick.ssupick_be.domain.oauth.dto.response;

import com.ssupick.ssupick_be.domain.user.entity.User;
import com.ssupick.ssupick_be.domain.user.enums.OnboardingStatus;

public record OAuthLoginResponse(
        Long userId,
        String accessToken,
        String refreshToken,
        boolean onboardingCompleted,   // 온보딩 완료 여부
        boolean aiImageGenerated       // selected=true 이미지 존재 여부
) {
    public static OAuthLoginResponse of(User user, String accessToken, String refreshToken,
                                        boolean aiImageGenerated) {
        return new OAuthLoginResponse(
                user.getId(),
                accessToken,
                refreshToken,
                user.getOnboardingStatus() == OnboardingStatus.COMPLETED,
                aiImageGenerated
        );
    }
}

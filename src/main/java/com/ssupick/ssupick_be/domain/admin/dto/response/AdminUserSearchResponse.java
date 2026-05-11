package com.ssupick.ssupick_be.domain.admin.dto.response;

import com.ssupick.ssupick_be.domain.user.entity.User;

public record AdminUserSearchResponse(
        Long userId,
        String name,
        String nickname,
        String email,
        String phoneNumber,
        String oauthProvider,
        String onboardingStatus,
        int remainingCouponCount
) {
    public static AdminUserSearchResponse from(User user) {
        return new AdminUserSearchResponse(
                user.getId(),
                user.getName(),
                user.getNickname(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getOauthProvider().name(),
                user.getOnboardingStatus().name(),
                user.getRemainingCouponCount()
        );
    }
}

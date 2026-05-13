package com.ssupick.ssupick_be.domain.admin.dto.response;

import com.ssupick.ssupick_be.domain.user.entity.User;

public record AdminGenerationCountSetResponse(
        Long userId,
        String nickname,
        int remainingGenerationCount
) {
    public static AdminGenerationCountSetResponse from(User user) {
        return new AdminGenerationCountSetResponse(
                user.getId(),
                user.getNickname(),
                user.getRemainingGenerationCount()
        );
    }
}

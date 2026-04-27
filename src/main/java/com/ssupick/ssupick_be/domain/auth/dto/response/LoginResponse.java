package com.ssupick.ssupick_be.domain.auth.dto.response;

public record LoginResponse(
        Long userId,
        String accessToken,
        String refreshToken
) {}

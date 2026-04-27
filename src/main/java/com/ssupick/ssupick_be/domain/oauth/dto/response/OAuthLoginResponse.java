package com.ssupick.ssupick_be.domain.oauth.dto.response;

public record OAuthLoginResponse(
        Long userId,
        String accessToken,
        String refreshToken
) {}

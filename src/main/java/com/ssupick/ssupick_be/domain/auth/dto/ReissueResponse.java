package com.ssupick.ssupick_be.domain.auth.dto;

public record ReissueResponse(
        String accessToken,
        String refreshToken
) {}

package com.ssupick.ssupick_be.domain.auth.dto.response;

public record ReissueResponse(
        String accessToken,
        String refreshToken
) {}

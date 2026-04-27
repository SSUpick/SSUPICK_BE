package com.ssupick.ssupick_be.common.jwt;

public record TokenIssuance(
        String accessToken,
        String refreshToken
) {}

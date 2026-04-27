package com.ssupick.ssupick_be.domain.auth.dto.request;

import com.ssupick.ssupick_be.domain.user.enums.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(

        @NotBlank(message = "테스트 유저 ID는 필수입니다.")
        String testUserId,

        @NotNull(message = "디바이스 타입은 필수입니다.")
        DeviceType deviceType
) {}

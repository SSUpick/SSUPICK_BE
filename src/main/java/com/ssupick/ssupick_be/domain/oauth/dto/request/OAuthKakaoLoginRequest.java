package com.ssupick.ssupick_be.domain.oauth.dto.request;

import com.ssupick.ssupick_be.domain.oauth.enums.RedirectType;
import com.ssupick.ssupick_be.domain.user.enums.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OAuthKakaoLoginRequest(

        @NotBlank(message = "인가 코드는 필수입니다.")
        String code,

        @NotNull(message = "디바이스 타입은 필수입니다.")
        DeviceType deviceType,

        @NotNull(message = "리다이렉트 타입은 필수입니다.")
        RedirectType redirectType
) {}

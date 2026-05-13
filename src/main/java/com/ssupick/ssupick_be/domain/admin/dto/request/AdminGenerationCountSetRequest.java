package com.ssupick.ssupick_be.domain.admin.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminGenerationCountSetRequest(

        @NotBlank(message = "닉네임은 필수입니다.")
        String nickname,

        @NotNull(message = "이미지 생성 횟수는 필수입니다.")
        @Min(value = 0, message = "이미지 생성 횟수는 0 이상이어야 합니다.")
        Integer remainingGenerationCount
) {}

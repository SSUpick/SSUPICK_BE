package com.ssupick.ssupick_be.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "닉네임 검증 요청")
public record ValidateNicknameRequest(

        @Schema(description = "검증할 닉네임", example = "숭실대 카리나")
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(max = 7, message = "닉네임은 공백 포함 7자 이하로 입력해주세요.")
        String nickname
) {
}

package com.ssupick.ssupick_be.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "닉네임 검증 응답")
public record ValidateNicknameResponse(

        @Schema(description = "닉네임 사용 가능 여부", example = "true")
        boolean available
) {
    public static ValidateNicknameResponse ofAvailable() {
        return new ValidateNicknameResponse(true);
    }
}

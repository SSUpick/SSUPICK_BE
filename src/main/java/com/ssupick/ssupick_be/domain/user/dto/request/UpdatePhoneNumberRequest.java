package com.ssupick.ssupick_be.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "전화번호 등록/수정 요청")
public record UpdatePhoneNumberRequest(

        @Schema(description = "결제에 사용할 전화번호", example = "01012345678")
        @NotBlank(message = "전화번호는 필수입니다.")
        @Pattern(regexp = "^01[0-9]{8,9}$", message = "전화번호는 하이픈 없이 10~11자리 숫자로 입력해주세요.")
        String phoneNumber
) {
}

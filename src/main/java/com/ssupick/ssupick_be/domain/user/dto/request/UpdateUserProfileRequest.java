package com.ssupick.ssupick_be.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "마이페이지 프로필 수정 요청")
public record UpdateUserProfileRequest(

        @Schema(description = "닉네임", example = "숭실대 카리나")
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(max = 10, message = "닉네임은 10자 이하로 입력해주세요.")
        String nickname,

        @Schema(description = "MBTI", example = "INTJ")
        @NotBlank(message = "MBTI는 필수입니다.")
        @Pattern(regexp = "^[EI][NS][TF][JP]$", message = "올바른 MBTI 형식이 아닙니다.")
        String mbti,

        @Schema(description = "어필 항목 (최대 3개)", example = "[\"고양이상\", \"키 160cm\", \"청순\"]")
        @NotNull(message = "어필 항목은 필수입니다.")
        @Size(min = 1, max = 3, message = "어필 항목은 1개 이상 3개 이하로 입력해주세요.")
        List<@NotBlank(message = "어필 항목 내용은 공백일 수 없습니다.")
        @Size(max = 8, message = "어필 항목은 8자 이하로 입력해주세요.") String> appeals,

        @Schema(description = "연락처 (인스타그램, 전화번호 등)", example = "@ssu_pick")
        @NotBlank(message = "연락처는 필수입니다.")
        @Size(min=2, max = 50, message = "연락처는 최소 2자, 최대 50자 이하로 입력해주세요.")
        String contact

) {
    public UpdateUserProfileCommand toCommand() {
        return new UpdateUserProfileCommand(nickname, mbti, appeals, contact);
    }
}

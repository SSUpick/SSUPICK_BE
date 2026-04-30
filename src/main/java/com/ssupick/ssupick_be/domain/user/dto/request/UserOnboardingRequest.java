package com.ssupick.ssupick_be.domain.user.dto.request;

import com.ssupick.ssupick_be.domain.user.enums.AppearanceStyle;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "유저 온보딩 프로필 등록 요청")
public record UserOnboardingRequest(

        @Schema(description = "닉네임", example = "숭실대 카리나")
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(max = 20, message = "닉네임은 20자 이하로 입력해주세요.")
        String nickname,

        @Schema(description = "MBTI", example = "ISTJ")
        @NotBlank(message = "MBTI는 필수입니다.")
        @Pattern(regexp = "^[EI][NS][TF][JP]$", message = "올바른 MBTI 형식이 아닙니다.")
        String mbti,

        @Schema(description = "외적 스타일 (PURE, CUTE, CHIC)", example = "CHIC")
        @NotNull(message = "외적 스타일은 필수입니다.")
        AppearanceStyle appearanceStyle,

        @Schema(description = "연락처 (인스타그램, 전화번호 등)", example = "@ssu_pick")
        @NotBlank(message = "연락처는 필수입니다.")
        @Size(max = 100, message = "연락처는 100자 이하로 입력해주세요.")
        String contact,

        @Schema(description = "어필 문구", example = "축제 같이 놀아요!")
        @NotBlank(message = "어필 문구는 필수입니다.")
        @Size(max = 100, message = "어필 문구는 100자 이하로 입력해주세요.")
        String appealMessage
) {}

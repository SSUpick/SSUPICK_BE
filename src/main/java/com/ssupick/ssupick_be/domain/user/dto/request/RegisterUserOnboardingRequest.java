package com.ssupick.ssupick_be.domain.user.dto.request;

import com.ssupick.ssupick_be.domain.user.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.util.List;

@Schema(description = "유저 온보딩 프로필 등록 요청")
public record RegisterUserOnboardingRequest(

        @Schema(description = "닉네임", example = "숭실대 카리나")
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(max = 20, message = "닉네임은 20자 이하로 입력해주세요.")
        String nickname,

        @Schema(description = "MBTI", example = "INTJ")
        @NotBlank(message = "MBTI는 필수입니다.")
        @Pattern(regexp = "^[EI][NS][TF][JP]$", message = "올바른 MBTI 형식이 아닙니다.")
        String mbti,

        @Schema(description = "상대방에게 어필할 항목 (최대 3개)", example = "[\"고양이상\", \"160cm\", \"청순\"]")
        @NotNull(message = "어필 항목은 필수입니다.")
        @Size(min = 1, max = 3, message = "어필 항목은 1개 이상 3개 이하로 입력해주세요.")
        List<@NotBlank(message = "어필 항목 내용은 공백일 수 없습니다.")
        @Size(max = 50, message = "어필 항목은 50자 이하로 입력해주세요.") String> appeals,

        @Schema(description = "연락처 (인스타그램, 전화번호 등)", example = "@ssu_pick")
        @NotBlank(message = "연락처는 필수입니다.")
        @Size(max = 100, message = "연락처는 100자 이하로 입력해주세요.")
        String contact,

        @Schema(description = "성별 (여자, 남자)", example = "MALE OR FEMALE")
        @NotNull(message = "성별은 필수입니다.")
        Gender gender
) {}

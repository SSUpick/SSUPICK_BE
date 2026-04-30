package com.ssupick.ssupick_be.domain.user.dto.response;

import com.ssupick.ssupick_be.domain.user.entity.User;
import com.ssupick.ssupick_be.domain.user.enums.AppearanceStyle;
import com.ssupick.ssupick_be.domain.user.enums.Gender;
import com.ssupick.ssupick_be.domain.user.enums.OnboardingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "유저 프로필 조회 응답")
public class UserProfileResponse {

    @Schema(description = "유저 ID", example = "1")
    private Long userId;

    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(description = "이메일", example = "hong@example.com")
    private String email;

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private String profileUrl;

    @Schema(description = "성별", example = "MALE")
    private Gender gender;

    @Schema(description = "나이", example = "25")
    private Integer age;

    @Schema(description = "온보딩 상태", example = "COMPLETED")
    private OnboardingStatus onboardingStatus;

    @Schema(description = "닉네임", example = "숭실대 카리나")
    private String nickname;

    @Schema(description = "MBTI", example = "ISTJ")
    private String mbti;

    @Schema(description = "외적 스타일", example = "CHIC")
    private AppearanceStyle appearanceStyle;

    @Schema(description = "연락처", example = "@ssu_pick")
    private String contact;

    @Schema(description = "어필 문구", example = "축제 같이 놀아요!")
    private String appealMessage;

    public static UserProfileResponse from(User user) {
        return UserProfileResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .profileUrl(user.getProfileUrl())
                .gender(user.getGender())
                .age(user.getAge())
                .onboardingStatus(user.getOnboardingStatus())
                .nickname(user.getNickname())
                .mbti(user.getMbti())
                .appearanceStyle(user.getAppearanceStyle())
                .contact(user.getContact())
                .appealMessage(user.getAppealMessage())
                .build();
    }
}

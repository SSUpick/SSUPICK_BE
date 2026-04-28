package com.ssupick.ssupick_be.domain.user.dto.response;

import com.ssupick.ssupick_be.domain.user.entity.User;
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

    @Schema(description = "온보딩 상태", example = "COMPLETE")
    private OnboardingStatus onboardingStatus;

    public static UserProfileResponse from(User user) {
        return UserProfileResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .profileUrl(user.getProfileUrl())
                .gender(user.getGender())
                .age(user.getAge())
                .onboardingStatus(user.getOnboardingStatus())
                .build();
    }
}

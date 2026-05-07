package com.ssupick.ssupick_be.domain.user.dto.response;

import com.ssupick.ssupick_be.domain.user.entity.ProfileView;
import com.ssupick.ssupick_be.domain.user.entity.User;
import com.ssupick.ssupick_be.domain.user.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "열람 유저 응답 (내가 열람한 / 나를 열람한 리스트용)")
public class GetUserViewResponse {

    @Schema(description = "유저 ID", example = "2")
    private Long userId;

    @Schema(description = "닉네임", example = "숭실대 차은우")
    private String nickname;

    @Schema(description = "성별", example = "MALE")
    private Gender gender;

    @Schema(description = "MBTI", example = "ESFP")
    private String mbti;

    @Schema(description = "어필 항목 목록", example = "[\"최대8글자입니다\"]")
    private List<String> appeals;

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private String profileUrl;

    @Schema(description = "열람 시각", example = "2025-05-07T14:30:00")
    private LocalDateTime viewedAt;

    // 내가 열람한 사람 — target 기준
    public static GetUserViewResponse ofViewed(ProfileView profileView) {
        User target = profileView.getTarget();
        return GetUserViewResponse.builder()
                .userId(target.getId())
                .nickname(target.getNickname())
                .gender(target.getGender())
                .mbti(target.getMbti())
                .appeals(target.getAppeals())
                .profileUrl(target.getProfileUrl())
                .viewedAt(profileView.getViewedAt())
                .build();
    }

    // 나를 열람한 사람 — viewer 기준
    public static GetUserViewResponse ofViewer(ProfileView profileView) {
        User viewer = profileView.getViewer();
        return GetUserViewResponse.builder()
                .userId(viewer.getId())
                .nickname(viewer.getNickname())
                .gender(viewer.getGender())
                .mbti(viewer.getMbti())
                .appeals(viewer.getAppeals())
                .profileUrl(viewer.getProfileUrl())
                .viewedAt(profileView.getViewedAt())
                .build();
    }
}

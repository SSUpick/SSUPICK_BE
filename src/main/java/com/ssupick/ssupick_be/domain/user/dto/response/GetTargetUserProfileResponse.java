package com.ssupick.ssupick_be.domain.user.dto.response;

import com.ssupick.ssupick_be.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "상대 유저 프로필 조회 응답")
public class GetTargetUserProfileResponse {

    @Schema(description = "유저 ID", example = "2")
    private Long userId;

    @Schema(description = "닉네임", example = "숭실대 차은우")
    private String nickname;

    @Schema(description = "MBTI", example = "INTJ")
    private String mbti;

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private String profileUrl;

    @Schema(description = "어필 항목 목록", example = "[\"고양이상\", \"160cm\", \"청순\"]")
    private List<String> appeals;

    @Schema(description = "연락처 (잠금 해제 후 노출)", example = "@ssu_pick")
    private String contact;

    public static GetTargetUserProfileResponse from(User user) {
        return GetTargetUserProfileResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .mbti(user.getMbti())
                .profileUrl(user.getProfileUrl())
                .appeals(user.getAppeals())
                .contact(user.getContact())
                .build();
    }
}

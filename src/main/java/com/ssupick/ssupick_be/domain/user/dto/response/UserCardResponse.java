package com.ssupick.ssupick_be.domain.user.dto.response;

import com.ssupick.ssupick_be.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "유저 카드 응답 (리스트 조회용)")
public class UserCardResponse {

    @Schema(description = "유저 ID", example = "1")
    private Long userId;

    @Schema(description = "닉네임", example = "숭실대 카리나")
    private String nickname;

    @Schema(description = "MBTI", example = "INTJ")
    private String mbti;

    @Schema(description = "어필 문구", example = "축제 같이 놀아요!")
    private String appealMessage;

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private String profileUrl;

    public static UserCardResponse from(User user) {
        return UserCardResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .mbti(user.getMbti())
                .appealMessage(user.getAppealMessage())
                .profileUrl(user.getProfileUrl())
                .build();
    }
}

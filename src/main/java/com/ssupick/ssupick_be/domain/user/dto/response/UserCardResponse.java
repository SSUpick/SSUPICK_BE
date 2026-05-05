package com.ssupick.ssupick_be.domain.user.dto.response;

import com.ssupick.ssupick_be.domain.user.entity.User;
import com.ssupick.ssupick_be.domain.user.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "유저 카드 응답 (리스트 조회용)")
public class UserCardResponse {

    @Schema(description = "유저 ID", example = "1")
    private Long userId;

    @Schema(description = "닉네임", example = "숭실대 카리나")
    private String nickname;

    @Schema(description = "유저 성별", example = "MALE OR FEMALE")
    private Gender gender;

    @Schema(description = "MBTI", example = "INTJ")
    private String mbti;

    @Schema(description = "어필 항목 목록", example = "[\"고양이상\", \"160cm\", \"청순\"]")
    private List<String> appeals;

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private String profileUrl;

    public static UserCardResponse from(User user) {
        return UserCardResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .gender(user.getGender())
                .mbti(user.getMbti())
                .appeals(user.getAppeals())
                .profileUrl(user.getProfileUrl())
                .build();
    }
}

package com.ssupick.ssupick_be.domain.oauth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoUserInfoResponse(

        Long id,

        @JsonProperty("kakao_account")
        KakaoAccount kakaoAccount
) {

    public String extractEmail() {
        return kakaoAccount != null ? kakaoAccount.email() : null;
    }

    public String extractNickname() {
        return kakaoAccount != null && kakaoAccount.profile() != null
                ? kakaoAccount.profile().nickname() : null;
    }

    public String extractProfileImageUrl() {
        return kakaoAccount != null && kakaoAccount.profile() != null
                ? kakaoAccount.profile().profileImageUrl() : null;
    }

    public record KakaoAccount(

            String email,

            @JsonProperty("profile")
            Profile profile
    ) {
        public record Profile(

                String nickname,

                @JsonProperty("profile_image_url")
                String profileImageUrl
        ) {}
    }
}

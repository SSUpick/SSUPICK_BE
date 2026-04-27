package com.ssupick.ssupick_be.domain.oauth.service;

import com.ssupick.ssupick_be.common.jwt.JwtService;
import com.ssupick.ssupick_be.common.jwt.TokenIssuance;
import com.ssupick.ssupick_be.domain.oauth.client.OAuthKakaoClient;
import com.ssupick.ssupick_be.domain.oauth.dto.KakaoTokenResponse;
import com.ssupick.ssupick_be.domain.oauth.dto.KakaoUserInfoResponse;
import com.ssupick.ssupick_be.domain.oauth.dto.request.OAuthKakaoLoginRequest;
import com.ssupick.ssupick_be.domain.oauth.dto.response.OAuthLoginResponse;
import com.ssupick.ssupick_be.domain.user.entity.User;
import com.ssupick.ssupick_be.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final OAuthKakaoClient oAuthKakaoClient;

    @Transactional
    public OAuthLoginResponse kakaoLogin(OAuthKakaoLoginRequest request) {
        KakaoTokenResponse kakaoToken = oAuthKakaoClient.getKakaoToken(request.code());
        KakaoUserInfoResponse userInfo = oAuthKakaoClient.getKakaoUserInfo(kakaoToken.accessToken());

        // DTO 파싱은 KakaoUserInfoResponse 내부에서 처리
        String kakaoId = userInfo.id().toString();
        String email = userInfo.extractEmail();
        String name = userInfo.extractNickname();
        String profileUrl = userInfo.extractProfileImageUrl();

        User user = userService.findOrRegisterKakaoUser(kakaoId, email, name, profileUrl, request.deviceType());

        TokenIssuance tokens = jwtService.issueTokens(user);
        return new OAuthLoginResponse(user.getId(), tokens.accessToken(), tokens.refreshToken());
    }

}

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

        // DTO 파싱은 oauth 레이어에서 처리 후 순수 값만 UserService로 전달
        KakaoUserInfoResponse.KakaoAccount account = userInfo.kakaoAccount();
        String kakaoId = userInfo.id().toString();
        String email = (account != null && account.email() != null) ? account.email() : null;
        String name = (account != null && account.profile() != null) ? account.profile().nickname() : null;
        String profileUrl = (account != null && account.profile() != null) ? account.profile().profileImageUrl() : null;

        User user = userService.findOrRegisterKakaoUser(kakaoId, email, name, profileUrl, request.deviceType());

        TokenIssuance tokens = jwtService.issueTokens(user);
        return new OAuthLoginResponse(user.getId(), tokens.accessToken(), tokens.refreshToken());
    }

}

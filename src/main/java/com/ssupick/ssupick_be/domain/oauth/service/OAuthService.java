package com.ssupick.ssupick_be.domain.oauth.service;

import com.ssupick.ssupick_be.common.jwt.JwtService;
import com.ssupick.ssupick_be.common.jwt.TokenIssuance;
import com.ssupick.ssupick_be.domain.aiimage.repository.AiImageRepository;
import com.ssupick.ssupick_be.domain.oauth.client.OAuthKakaoClient;
import com.ssupick.ssupick_be.domain.oauth.dto.KakaoTokenResponse;
import com.ssupick.ssupick_be.domain.oauth.dto.KakaoUserInfoResponse;
import com.ssupick.ssupick_be.domain.oauth.dto.request.OAuthKakaoLoginRequest;
import com.ssupick.ssupick_be.domain.oauth.dto.response.OAuthLoginResponse;
import com.ssupick.ssupick_be.domain.user.entity.User;
import com.ssupick.ssupick_be.domain.user.enums.DeviceType;
import com.ssupick.ssupick_be.domain.user.service.RandomNicknameGenerator;
import com.ssupick.ssupick_be.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final OAuthKakaoClient oAuthKakaoClient;
    private final AiImageRepository aiImageRepository;
    private final RandomNicknameGenerator randomNicknameGenerator;

    // 외부 API 호출은 트랜잭션 밖에서 수행합니다.
    // DB 작업은 UserService.findOrRegisterKakaoUser의 @Transactional에 위임합니다.
    public OAuthLoginResponse kakaoLogin(OAuthKakaoLoginRequest request) {
        // 트랜잭션 밖: 카카오 외부 API 호출
        KakaoTokenResponse kakaoToken = oAuthKakaoClient.getKakaoToken(request.code(), request.redirectType());
        KakaoUserInfoResponse userInfo = oAuthKakaoClient.getKakaoUserInfo(kakaoToken.accessToken());

        String kakaoId = userInfo.id().toString();
        String email = userInfo.extractEmail();
        String name = userInfo.extractNickname();
        String profileUrl = userInfo.extractProfileImageUrl();

        // 트랜잭션 안: 유저 조회/등록 + 토큰 발급
        return processLogin(kakaoId, email, name, profileUrl, request.deviceType());
    }

    // 유저 조회/등록 + JWT 발급을 하나의 트랜잭션으로 처리합니다.
    private OAuthLoginResponse processLogin(
            String kakaoId, String email, String name, String profileUrl, DeviceType deviceType
    ) {
        User user = userService.findOrRegisterKakaoUser(kakaoId, email, name, profileUrl, deviceType);
        boolean firstLogin = user.isFirstLogin();
        TokenIssuance tokens = jwtService.issueTokens(user);
        boolean aiImageGenerated = aiImageRepository.existsByUserAndSelectedTrue(user);
        String randomNickname = randomNicknameGenerator.generate();
        user.completeFirstLogin();
        return OAuthLoginResponse.of(
                user,
                tokens.accessToken(),
                tokens.refreshToken(),
                aiImageGenerated,
                randomNickname,
                firstLogin
        );
    }
}

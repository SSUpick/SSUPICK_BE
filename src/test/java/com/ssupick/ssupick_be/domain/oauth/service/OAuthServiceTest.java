package com.ssupick.ssupick_be.domain.oauth.service;

import com.ssupick.ssupick_be.common.jwt.JwtService;
import com.ssupick.ssupick_be.common.jwt.TokenIssuance;
import com.ssupick.ssupick_be.domain.aiimage.repository.AiImageRepository;
import com.ssupick.ssupick_be.domain.oauth.client.OAuthKakaoClient;
import com.ssupick.ssupick_be.domain.oauth.dto.KakaoTokenResponse;
import com.ssupick.ssupick_be.domain.oauth.dto.KakaoUserInfoResponse;
import com.ssupick.ssupick_be.domain.oauth.dto.request.OAuthKakaoLoginRequest;
import com.ssupick.ssupick_be.domain.oauth.dto.response.OAuthLoginResponse;
import com.ssupick.ssupick_be.domain.oauth.enums.RedirectType;
import com.ssupick.ssupick_be.domain.user.entity.User;
import com.ssupick.ssupick_be.domain.user.enums.DeviceType;
import com.ssupick.ssupick_be.domain.user.service.RandomNicknameGenerator;
import com.ssupick.ssupick_be.domain.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private OAuthKakaoClient oAuthKakaoClient;

    @Mock
    private AiImageRepository aiImageRepository;

    @Mock
    private RandomNicknameGenerator randomNicknameGenerator;

    @InjectMocks
    private OAuthService oAuthService;

    private static final OAuthKakaoLoginRequest REQUEST = new OAuthKakaoLoginRequest(
            "auth-code", DeviceType.IOS, RedirectType.PROD
    );

    private static final KakaoTokenResponse KAKAO_TOKEN = new KakaoTokenResponse(
            "kakao-access-token", "bearer", "kakao-refresh-token", 3600L, 86400L
    );

    // 카카오 로그인 성공 시 accessToken, refreshToken, onboardingCompleted가 포함된 응답을 반환합니다.
    @Test
    void kakaoLogin_returnsOAuthLoginResponseOnSuccess() {
        User user = User.createTestUser("test-user", DeviceType.IOS);
        KakaoUserInfoResponse userInfo = new KakaoUserInfoResponse(
                12345L,
                new KakaoUserInfoResponse.KakaoAccount("test@test.com",
                        new KakaoUserInfoResponse.KakaoAccount.Profile("테스트", "https://profile.jpg"))
        );

        when(oAuthKakaoClient.getKakaoToken("auth-code", RedirectType.PROD)).thenReturn(KAKAO_TOKEN);
        when(oAuthKakaoClient.getKakaoUserInfo("kakao-access-token")).thenReturn(userInfo);
        when(randomNicknameGenerator.generate()).thenReturn("랜덤닉네임");
        when(userService.findOrRegisterKakaoUser(
                "12345", "test@test.com", "테스트", "https://profile.jpg", DeviceType.IOS, "랜덤닉네임"
        )).thenReturn(user);
        when(jwtService.issueTokens(user)).thenReturn(new TokenIssuance("access-token", "refresh-token"));
        when(aiImageRepository.existsByUserAndSelectedTrue(user)).thenReturn(false);

        OAuthLoginResponse response = oAuthService.kakaoLogin(REQUEST);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.onboardingCompleted()).isFalse();
        assertThat(response.aiImageGenerated()).isFalse();
    }

    // 카카오 API 호출 후 DB 작업은 UserService에 위임하는지 검증합니다.
    @Test
    void kakaoLogin_delegatesDbWorkToUserService() {
        User user = User.createTestUser("test-user", DeviceType.IOS);
        KakaoUserInfoResponse userInfo = new KakaoUserInfoResponse(
                12345L,
                new KakaoUserInfoResponse.KakaoAccount("test@test.com",
                        new KakaoUserInfoResponse.KakaoAccount.Profile("테스트", "https://profile.jpg"))
        );

        when(oAuthKakaoClient.getKakaoToken(any(), any())).thenReturn(KAKAO_TOKEN);
        when(oAuthKakaoClient.getKakaoUserInfo(any())).thenReturn(userInfo);
        when(randomNicknameGenerator.generate()).thenReturn("랜덤닉네임");
        when(userService.findOrRegisterKakaoUser(any(), any(), any(), any(), any(), any())).thenReturn(user);
        when(jwtService.issueTokens(user)).thenReturn(new TokenIssuance("access-token", "refresh-token"));
        when(aiImageRepository.existsByUserAndSelectedTrue(user)).thenReturn(false);

        oAuthService.kakaoLogin(REQUEST);

        verify(userService).findOrRegisterKakaoUser(
                "12345", "test@test.com", "테스트", "https://profile.jpg", DeviceType.IOS, "랜덤닉네임"
        );
        verify(jwtService).issueTokens(user);
    }

    // 카카오 토큰 발급 실패 시 UserService를 호출하지 않습니다.
    @Test
    void kakaoLogin_doesNotCallUserServiceWhenKakaoTokenFails() {
        when(oAuthKakaoClient.getKakaoToken(any(), any())).thenThrow(new RuntimeException("카카오 토큰 발급 실패"));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> oAuthService.kakaoLogin(REQUEST))
                .isInstanceOf(RuntimeException.class);

        verify(userService, never()).findOrRegisterKakaoUser(anyString(), any(), any(), any(), any(), any());
    }

    // 카카오 유저 정보 조회 실패 시 UserService를 호출하지 않습니다.
    @Test
    void kakaoLogin_doesNotCallUserServiceWhenUserInfoFails() {
        when(oAuthKakaoClient.getKakaoToken(any(), any())).thenReturn(KAKAO_TOKEN);
        when(oAuthKakaoClient.getKakaoUserInfo(any())).thenThrow(new RuntimeException("카카오 유저 정보 조회 실패"));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> oAuthService.kakaoLogin(REQUEST))
                .isInstanceOf(RuntimeException.class);

        verify(userService, never()).findOrRegisterKakaoUser(anyString(), any(), any(), any(), any(), any());
    }

    // AI 이미지가 생성된 경우 aiImageGenerated가 true로 반환됩니다.
    @Test
    void kakaoLogin_returnsAiImageGeneratedTrueWhenExists() {
        User user = User.createTestUser("test-user", DeviceType.IOS);
        KakaoUserInfoResponse userInfo = new KakaoUserInfoResponse(
                12345L,
                new KakaoUserInfoResponse.KakaoAccount("test@test.com",
                        new KakaoUserInfoResponse.KakaoAccount.Profile("테스트", "https://profile.jpg"))
        );

        when(oAuthKakaoClient.getKakaoToken(any(), any())).thenReturn(KAKAO_TOKEN);
        when(oAuthKakaoClient.getKakaoUserInfo(any())).thenReturn(userInfo);
        when(randomNicknameGenerator.generate()).thenReturn("랜덤닉네임");
        when(userService.findOrRegisterKakaoUser(any(), any(), any(), any(), any(), any())).thenReturn(user);
        when(jwtService.issueTokens(user)).thenReturn(new TokenIssuance("access-token", "refresh-token"));
        when(aiImageRepository.existsByUserAndSelectedTrue(user)).thenReturn(true);

        OAuthLoginResponse response = oAuthService.kakaoLogin(REQUEST);

        assertThat(response.aiImageGenerated()).isTrue();
    }
}

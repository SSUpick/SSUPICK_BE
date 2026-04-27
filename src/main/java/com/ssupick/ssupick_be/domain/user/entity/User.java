package com.ssupick.ssupick_be.domain.user.entity;

import com.ssupick.ssupick_be.common.base.BaseEntity;
import com.ssupick.ssupick_be.domain.user.enums.DeviceType;
import com.ssupick.ssupick_be.domain.user.enums.Gender;
import com.ssupick.ssupick_be.domain.user.enums.OAuthProvider;
import com.ssupick.ssupick_be.domain.user.enums.OnboardingStatus;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "user")
@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", length = 50)
    private String email;

    @Column(name = "name", length = 30)
    private String name;

    @Column(name = "profile_url", length = 255)
    private String profileUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", length = 20, nullable = false)
    private DeviceType deviceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "onboarding_status", length = 30, nullable = false)
    private OnboardingStatus onboardingStatus;

    @Column(name = "oauth_id", length = 255, nullable = false)
    private String oauthId;

    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider", length = 20, nullable = false)
    private OAuthProvider oauthProvider;

    @Column(name = "refresh_token", length = 512)
    private String refreshToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;

    @Column(name = "age")
    private Integer age;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    // 카카오 신규 유저 생성
    public static User createKakaoUser(
            String oauthId,
            String email,
            String name,
            String profileUrl,
            DeviceType deviceType
    ) {
        return User.builder()
                .oauthId(oauthId)
                .oauthProvider(OAuthProvider.KAKAO)
                .email(email)
                .name(name)
                .profileUrl(profileUrl)
                .deviceType(deviceType)
                .onboardingStatus(OnboardingStatus.INCOMPLETE)
                .deleted(false)
                .build();
    }

    // 테스트 유저 생성 (로컬 전용)
    public static User createTestUser(String testUserId, DeviceType deviceType) {
        return User.builder()
                .oauthId(testUserId)
                .oauthProvider(OAuthProvider.TEST)
                .email(testUserId + "@test.com")
                .name("테스트유저_" + testUserId)
                .deviceType(deviceType)
                .onboardingStatus(OnboardingStatus.INCOMPLETE)
                .deleted(false)
                .build();
    }

    // 탈퇴한 유저 재가입 처리 — null이면 기존 값 유지
    public void restore(String email, String name, String profileUrl, DeviceType deviceType) {
        if (email != null) this.email = email;
        if (name != null) this.name = name;
        if (profileUrl != null) this.profileUrl = profileUrl;
        this.deviceType = deviceType;
        this.onboardingStatus = OnboardingStatus.INCOMPLETE;
        this.deleted = false;
    }

    // 로그아웃 처리 — 리프레시 토큰 무효화
    public void logout() {
        this.refreshToken = null;
    }

    // 회원 탈퇴 처리
    public void withdraw() {
        this.deleted = true;
        this.refreshToken = null;
    }

    // 리프레시 토큰 업데이트
    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

}

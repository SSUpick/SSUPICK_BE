package com.ssupick.ssupick_be.domain.user.entity;

import com.ssupick.ssupick_be.common.base.BaseEntity;
import com.ssupick.ssupick_be.domain.user.enums.DeviceType;
import com.ssupick.ssupick_be.domain.user.enums.Gender;
import com.ssupick.ssupick_be.domain.user.enums.OAuthProvider;
import com.ssupick.ssupick_be.domain.user.enums.OnboardingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.stream.Stream;

@Table(name = "user")
@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

    @Column(name = "nickname", length = 20)
    private String nickname;

    @Column(name = "mbti", length = 4)
    private String mbti;

    @Column(name = "contact", length = 100)
    private String contact;

    @Column(name = "appeal1", length = 50)
    private String appeal1;

    @Column(name = "appeal2", length = 50)
    private String appeal2;

    @Column(name = "appeal3", length = 50)
    private String appeal3;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    // AI 이미지 생성 잔여 횟수 (최초 3회, 서버에서만 관리)
    @Builder.Default
    @Column(name = "remaining_generation_count", nullable = false)
    private int remainingGenerationCount = 3;

    // 카카오 신규 유저 생성
    public static User createKakaoUser(
            String oauthId, String email, String name, String profileUrl, DeviceType deviceType
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

    // 온보딩 프로필 등록 — 어필 항목 최대 3개 (인덱스 초과분은 null)
    public void completeOnboarding(String nickname, String mbti, String contact, List<String> appeals, Gender gender) {
        this.nickname = nickname;
        this.mbti = mbti;
        this.contact = contact;
        this.appeal1 = appeals.size() >= 1 ? appeals.get(0) : null;
        this.appeal2 = appeals.size() >= 2 ? appeals.get(1) : null;
        this.appeal3 = appeals.size() >= 3 ? appeals.get(2) : null;
        this.gender = gender;
        this.onboardingStatus = OnboardingStatus.COMPLETED;
    }

    // appeal1~3을 List로 반환 — null 항목 제외
    public List<String> getAppeals() {
        return Stream.of(appeal1, appeal2, appeal3)
                .filter(a -> a != null && !a.isBlank())
                .toList();
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

    // AI 이미지 생성 횟수 차감 — 0 이하면 예외
    public void decreaseGenerationCount() {
        if (this.remainingGenerationCount <= 0) {
            throw new IllegalStateException("이미지 생성 횟수가 부족합니다.");
        }
        this.remainingGenerationCount--;
    }

    // AI 이미지 생성 실패 시 횟수 복구 (최대 3 초과 방지)
    public void restoreGenerationCount() {
        if (this.remainingGenerationCount < 3) {
            this.remainingGenerationCount++;
        }
    }

    // 최종 선택 이미지를 프로필 URL로 확정
    public void updateProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }
}

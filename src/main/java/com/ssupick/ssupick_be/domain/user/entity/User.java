package com.ssupick.ssupick_be.domain.user.entity;

import com.ssupick.ssupick_be.common.base.BaseEntity;
import com.ssupick.ssupick_be.domain.user.enums.DeviceType;
import com.ssupick.ssupick_be.domain.user.enums.Gender;
import com.ssupick.ssupick_be.domain.user.enums.OAuthProvider;
import com.ssupick.ssupick_be.domain.user.enums.OnboardingStatus;
import com.ssupick.ssupick_be.domain.user.dto.request.UpdateUserProfileCommand;
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

    @Column(name = "profile_url", length = 512)
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

    @Column(name = "nickname", length = 7)
    private String nickname;

    @Column(name = "mbti", length = 4)
    private String mbti;

    @Column(name = "contact", length = 50)
    private String contact;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "appeal1", length = 8)
    private String appeal1;

    @Column(name = "appeal2", length = 8)
    private String appeal2;

    @Column(name = "appeal3", length = 8)
    private String appeal3;

    // AI 이미지 생성 잔여 횟수 (최초 3회, 서버에서만 관리)
    @Builder.Default
    @Column(name = "remaining_generation_count", nullable = false)
    private int remainingGenerationCount = 3;

    // 남은 쿠폰 개수
    @Builder.Default
    @Column(name = "remaining_coupon_count", nullable = false)
    private int remainingCouponCount = 0;

    @Builder.Default
    @Column(name = "first_login", nullable = false)
    private boolean firstLogin = true;

    // 카카오 신규 유저 생성
    public static User createKakaoUser(
            String oauthId, String email, String name, String profileUrl, DeviceType deviceType, String randomNickname
    ) {
        return createKakaoUser(oauthId, email, name, profileUrl, deviceType, randomNickname, 3);
    }

    public static User createKakaoUser(
            String oauthId, String email, String name, String profileUrl, DeviceType deviceType,
            String randomNickname, int remainingGenerationCount
    ) {
        return User.builder()
                .oauthId(oauthId)
                .oauthProvider(OAuthProvider.KAKAO)
                .email(email)
                .name(name)
                .profileUrl(null)
                .deviceType(deviceType)
                .onboardingStatus(OnboardingStatus.INCOMPLETE)
                .nickname(randomNickname)
                .remainingGenerationCount(remainingGenerationCount)
                .build();
    }

    // 테스트 유저 생성 (로컬 전용)
    public static User createTestUser(String testUserId, DeviceType deviceType) {
        return createTestUser(testUserId, deviceType, "테스트닉네임");
    }

    public static User createTestUser(String testUserId, DeviceType deviceType, String randomNickname) {
        return User.builder()
                .oauthId(testUserId)
                .oauthProvider(OAuthProvider.TEST)
                .email(testUserId + "@test.com")
                .name("테스트유저_" + testUserId)
                .deviceType(deviceType)
                .onboardingStatus(OnboardingStatus.INCOMPLETE)
                .nickname(randomNickname)
                .build();
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

    // 마이페이지 프로필 수정 — 온보딩 완료 이후 수정 가능
    public void updateProfile(UpdateUserProfileCommand command) {
        this.nickname = command.nickname();
        this.mbti = command.mbti();
        this.contact = command.contact();
        this.appeal1 = command.appeals().size() >= 1 ? command.appeals().get(0) : null;
        this.appeal2 = command.appeals().size() >= 2 ? command.appeals().get(1) : null;
        this.appeal3 = command.appeals().size() >= 3 ? command.appeals().get(2) : null;
    }

    // 결제에 사용할 전화번호 등록/수정
    public void updatePhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // 로그아웃 처리 — 리프레시 토큰 무효화
    public void logout() {
        this.refreshToken = null;
    }

    // 리프레시 토큰 업데이트
    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void completeFirstLogin() {
        this.firstLogin = false;
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

    // 관리자 수동 설정
    public void updateRemainingGenerationCount(int remainingGenerationCount) {
        this.remainingGenerationCount = remainingGenerationCount;
    }

    // 최종 선택 이미지를 프로필 URL로 확정
    public void updateProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    // 쿠폰 충전
    public void increaseCouponCount(int count) {
        this.remainingCouponCount += count;
    }

    // 프로필 열람 쿠폰 차감
    public void decreaseCouponCount() {
        if (this.remainingCouponCount <= 0) {
            throw new IllegalStateException("프로필 조회 쿠폰이 부족합니다.");
        }
        this.remainingCouponCount--;
    }
}

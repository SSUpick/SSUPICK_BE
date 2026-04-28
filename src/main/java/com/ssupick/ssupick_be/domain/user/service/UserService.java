package com.ssupick.ssupick_be.domain.user.service;

import com.ssupick.ssupick_be.common.exception.GeneralException;
import com.ssupick.ssupick_be.common.status.ErrorStatus;
import com.ssupick.ssupick_be.domain.user.dto.request.UserOnboardingRequest;
import com.ssupick.ssupick_be.domain.user.dto.response.UserProfileResponse;
import com.ssupick.ssupick_be.domain.user.entity.User;
import com.ssupick.ssupick_be.domain.user.enums.DeviceType;
import com.ssupick.ssupick_be.domain.user.enums.OAuthProvider;
import com.ssupick.ssupick_be.domain.user.enums.OnboardingStatus;
import com.ssupick.ssupick_be.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // ───────────────────────────── 공통 내부 헬퍼 ─────────────────────────────

    // userId로 유저 조회 — 없으면 예외 (내부 전용)
    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
    }

    // ───────────────────────────── Public API ─────────────────────────────────

    // 카카오 유저 조회 후 없으면 신규 생성, 탈퇴 유저면 복구
    @Transactional
    public User findOrRegisterKakaoUser(
            String kakaoId, String email, String name, String profileUrl, DeviceType deviceType
    ) {
        return userRepository.findByOauthIdAndOauthProvider(kakaoId, OAuthProvider.KAKAO)
                .map(user -> {
                    if (user.isDeleted())
                        user.restore(email, name, profileUrl, deviceType);
                    return user;
                })
                .orElseGet(() ->
                        userRepository.save(User.createKakaoUser(kakaoId, email, name, profileUrl, deviceType))
                );
    }

    // 테스트 유저 조회 후 없으면 신규 생성
    @Transactional
    public User findOrCreateTestUser(String testUserId, DeviceType deviceType) {
        return userRepository.findByOauthIdAndOauthProvider(testUserId, OAuthProvider.TEST)
                .orElseGet(() -> userRepository.save(User.createTestUser(testUserId, deviceType)));
    }

    // 유저 프로필 조회 — Controller에 Entity 노출 방지
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        return UserProfileResponse.from(getUserOrThrow(userId));
    }

    // 온보딩 프로필 등록 — 중복 등록 방어
    @Transactional
    public void registerOnboarding(Long userId, UserOnboardingRequest request) {
        User user = getUserOrThrow(userId);
        if (user.getOnboardingStatus() == OnboardingStatus.COMPLETED) {
            throw new GeneralException(ErrorStatus.ONBOARDING_ALREADY_COMPLETED);
        }
        user.completeOnboarding(request.nickname(), request.mbti(), request.appearanceStyle(), request.contact());
    }

    // 엔티티 변경이 필요한 경우 사용 (dirty checking 보장)
    @Transactional
    public User findByIdForUpdate(Long userId) {
        return getUserOrThrow(userId);
    }

}

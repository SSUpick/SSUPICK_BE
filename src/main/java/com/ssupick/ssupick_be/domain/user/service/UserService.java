package com.ssupick.ssupick_be.domain.user.service;

import com.ssupick.ssupick_be.common.exception.GeneralException;
import com.ssupick.ssupick_be.common.status.ErrorStatus;
import com.ssupick.ssupick_be.domain.user.dto.request.UserOnboardingRequest;
import com.ssupick.ssupick_be.domain.user.dto.response.TargetUserProfileResponse;
import com.ssupick.ssupick_be.domain.user.dto.response.UserCardResponse;
import com.ssupick.ssupick_be.domain.user.dto.response.UserProfileResponse;
import com.ssupick.ssupick_be.domain.user.entity.User;
import com.ssupick.ssupick_be.domain.user.enums.DeviceType;
import com.ssupick.ssupick_be.domain.user.enums.OAuthProvider;
import com.ssupick.ssupick_be.domain.user.enums.OnboardingStatus;
import com.ssupick.ssupick_be.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    // userId로 활성 유저 조회 — 탈퇴 유저 자동 차단 (내부 전용)
    private User getActiveUserOrThrow(Long userId) {
        return userRepository.findByIdAndDeletedFalse(userId)
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

    // 상대 유저 프로필 조회 — 탈퇴 유저 및 온보딩 미완료 유저 접근 차단
    @Transactional(readOnly = true)
    public TargetUserProfileResponse getTargetUserProfile(Long targetUserId) {
        User target = getActiveUserOrThrow(targetUserId);
        if (target.getOnboardingStatus() != OnboardingStatus.COMPLETED) {
            throw new GeneralException(ErrorStatus.USER_ONBOARDING_INCOMPLETE);
        }
        return TargetUserProfileResponse.from(target);
    }

    // 유저 프로필 조회 — Controller에 Entity 노출 방지
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(Long userId) {
        return UserProfileResponse.from(getUserOrThrow(userId));
    }

    // 온보딩 프로필 등록 — 중복 등록 방어 + appeals 이중 방어
    @Transactional
    public void registerOnboarding(Long userId, UserOnboardingRequest request) {
        User user = getUserOrThrow(userId);
        if (user.getOnboardingStatus() == OnboardingStatus.COMPLETED) {
            throw new GeneralException(ErrorStatus.ONBOARDING_ALREADY_COMPLETED);
        }
        if (request.appeals().stream().anyMatch(a -> a == null || a.isBlank())) {
            throw new GeneralException(ErrorStatus.INVALID_APPEAL_CONTENT);
        }
        user.completeOnboarding(request.nickname(), request.mbti(), request.contact(), request.appeals());
    }

    // 유저 카드 리스트 조회 — 온보딩 완료 유저, 본인 제외
    @Transactional(readOnly = true)
    public List<UserCardResponse> getUserCardList(Long userId) {
        return userRepository.findAllByOnboardingStatusAndDeletedFalseAndIdNot(
                        OnboardingStatus.COMPLETED, userId)
                .stream()
                .map(UserCardResponse::from)
                .toList();
    }

    // 엔티티 변경이 필요한 경우 사용 — AuthService에서 logout/withdraw/reissue 시 dirty checking 보장
    @Transactional
    public User findByIdForUpdate(Long userId) {
        return getUserOrThrow(userId);
    }

}

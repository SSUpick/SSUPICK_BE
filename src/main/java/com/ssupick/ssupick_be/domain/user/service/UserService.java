package com.ssupick.ssupick_be.domain.user.service;

import com.ssupick.ssupick_be.common.exception.GeneralException;
import com.ssupick.ssupick_be.common.status.ErrorStatus;
import com.ssupick.ssupick_be.domain.aiimage.service.AiImageService;
import com.ssupick.ssupick_be.domain.user.dto.request.RegisterUserOnboardingRequest;
import com.ssupick.ssupick_be.domain.user.dto.request.UpdateUserProfileRequest;
import com.ssupick.ssupick_be.domain.user.dto.response.*;
import com.ssupick.ssupick_be.domain.user.entity.ProfileView;
import com.ssupick.ssupick_be.domain.user.entity.User;
import com.ssupick.ssupick_be.domain.user.enums.DeviceType;
import com.ssupick.ssupick_be.domain.user.enums.OAuthProvider;
import com.ssupick.ssupick_be.domain.user.enums.OnboardingStatus;
import com.ssupick.ssupick_be.domain.user.repository.ProfileViewRepository;
import com.ssupick.ssupick_be.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AiImageService aiImageService;
    private final UserRepository userRepository;
    private final ProfileViewRepository profileViewRepository;

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

    // 상대 유저 프로필 조회 — 자기 자신 열람 방지 + 열람 기록 upsert
    @Transactional
    public GetTargetUserProfileResponse getTargetUserProfile(Long viewerId, Long targetId) {
        if (viewerId.equals(targetId)) {
            throw new GeneralException(ErrorStatus.SELF_VIEW_NOT_ALLOWED);
        }
        User viewer = getActiveUserOrThrow(viewerId);
        User target = getActiveUserOrThrow(targetId);
        if (target.getOnboardingStatus() != OnboardingStatus.COMPLETED) {
            throw new GeneralException(ErrorStatus.USER_ONBOARDING_INCOMPLETE);
        }
        if (viewer.getRemainingCouponCount() <= 0) {
            throw new GeneralException(ErrorStatus.PROFILE_VIEW_COUPON_REQUIRED);
        }
        viewer.decreaseCouponCount();

        profileViewRepository.findByViewerAndTarget(viewer, target)
                .ifPresentOrElse(
                        ProfileView::updateViewedAt,
                        () -> profileViewRepository.save(new ProfileView(viewer, target))
                );
        return GetTargetUserProfileResponse.from(target);
    }

    // 유저 프로필 조회 — Controller에 Entity 노출 방지
    @Transactional(readOnly = true)
    public GetUserProfileResponse getUserProfile(Long userId) {
        return GetUserProfileResponse.from(getActiveUserOrThrow(userId));
    }

    // 온보딩 프로필 등록 — 중복 등록 방어 + appeals 이중 방어
    @Transactional
    public void registerUserOnboarding(Long userId, RegisterUserOnboardingRequest request) {
        User user = getActiveUserOrThrow(userId);
        if (user.getOnboardingStatus() == OnboardingStatus.COMPLETED) {
            throw new GeneralException(ErrorStatus.ONBOARDING_ALREADY_COMPLETED);
        }
        if (request.appeals() == null || request.appeals().stream().anyMatch(a -> a == null || a.isBlank())) {
            throw new GeneralException(ErrorStatus.INVALID_APPEAL_CONTENT);
        }
        user.completeOnboarding(request.nickname(), request.mbti(), request.contact(), request.appeals(), request.gender());
    }

    // 유저 카드 리스트 조회 — 온보딩 완료 유저, 본인 제외
    @Transactional(readOnly = true)
    public List<GetUserCardResponse> getUserCardList(Long userId) {
        return userRepository.findAllByOnboardingStatusAndDeletedFalseAndIdNot(
                        OnboardingStatus.COMPLETED, userId)
                .stream()
                .map(GetUserCardResponse::from)
                .toList();
    }

    // 내가 열람한 / 나를 열람한 유저 목록 통합 조회 — 최신순
    @Transactional(readOnly = true)
    public GetProfileViewListResponse getProfileViewList(Long userId) {
        User user = getActiveUserOrThrow(userId);
        // 내가 열람한 사람 목록
        List<GetUserViewResponse> viewedUsers = profileViewRepository.findByViewerOrderByViewedAtDesc(user)
                .stream()
                .map(GetUserViewResponse::ofViewed)
                .toList();
        // 나를 열람한 사람 목록
        List<GetUserViewResponse> viewerUsers = profileViewRepository.findByTargetOrderByViewedAtDesc(user)
                .stream()
                .map(GetUserViewResponse::ofViewer)
                .toList();
        return GetProfileViewListResponse.of(viewedUsers, viewerUsers);
    }

    // 마이페이지 프로필 수정 — 온보딩 완료 유저만 수정 가능
    @Transactional
    public void updateUserProfile(Long userId, UpdateUserProfileRequest request) {
        User user = getActiveUserOrThrow(userId);
        if (user.getOnboardingStatus() != OnboardingStatus.COMPLETED) {
            throw new GeneralException(ErrorStatus.USER_ONBOARDING_INCOMPLETE);
        }
        user.updateProfile(request.toCommand());
    }

    // AuthService 전용 — logout/withdraw/reissue 시 사용
    // 탈퇴 유저도 조회 가능해야 하므로 의도적으로 deletedFalse 미적용
    @Transactional
    public User findByIdForUpdate(Long userId) {
        return getUserOrThrow(userId);
    }

    public void withdraw(Long userId) {
        User user = getUserOrThrow(userId);
        aiImageService.deleteByUser(user);
        deleteProfileView(user);
        userRepository.delete(user);
    }
    // 유저 관련 열람 기록 삭제
    public void deleteProfileView(User user) {
        profileViewRepository.deleteByViewerOrTarget(user, user);
    }

}

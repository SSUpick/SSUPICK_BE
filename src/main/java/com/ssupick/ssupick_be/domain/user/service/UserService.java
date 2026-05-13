package com.ssupick.ssupick_be.domain.user.service;

import com.ssupick.ssupick_be.common.exception.GeneralException;
import com.ssupick.ssupick_be.common.filter.ProfanityFilter;
import com.ssupick.ssupick_be.common.status.ErrorStatus;
import com.ssupick.ssupick_be.domain.admin.repository.AdminCouponAdjustmentRepository;
import com.ssupick.ssupick_be.domain.aiimage.service.AiImageService;
import com.ssupick.ssupick_be.domain.bank.repository.BankDepositEventRepository;
import com.ssupick.ssupick_be.domain.payment.service.PaymentService;
import com.ssupick.ssupick_be.domain.user.dto.request.RegisterUserOnboardingRequest;
import com.ssupick.ssupick_be.domain.user.dto.request.UpdatePhoneNumberRequest;
import com.ssupick.ssupick_be.domain.user.dto.request.UpdateUserProfileRequest;
import com.ssupick.ssupick_be.domain.user.dto.request.ValidateNicknameRequest;
import com.ssupick.ssupick_be.domain.user.dto.response.*;
import com.ssupick.ssupick_be.domain.user.entity.User;
import com.ssupick.ssupick_be.domain.user.entity.WithdrawUser;
import com.ssupick.ssupick_be.domain.user.enums.DeviceType;
import com.ssupick.ssupick_be.domain.user.enums.OAuthProvider;
import com.ssupick.ssupick_be.domain.user.enums.OnboardingStatus;
import com.ssupick.ssupick_be.domain.user.repository.ProfileViewRepository;
import com.ssupick.ssupick_be.domain.user.repository.UserRepository;
import com.ssupick.ssupick_be.domain.user.repository.WithdrawUserRepository;
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
    private final ProfanityFilter profanityFilter;
    private final PaymentService paymentService;
    private final AdminCouponAdjustmentRepository adminCouponAdjustmentRepository;
    private final BankDepositEventRepository bankDepositEventRepository;
    private final WithdrawUserRepository withdrawUserRepository;

    // ───────────────────────────── 공통 내부 헬퍼 ─────────────────────────────

    // userId로 유저 조회 — 없으면 예외 (내부 전용)
    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
    }

    private void validateNickname(String nickname, Long excludeUserId) {
        profanityFilter.validateNickname(nickname);

        boolean exists = excludeUserId == null
                ? userRepository.existsByNickname(nickname)
                : userRepository.existsByNicknameAndIdNot(nickname, excludeUserId);
        if (exists) {
            throw new GeneralException(ErrorStatus.NICKNAME_ALREADY_EXISTS);
        }
    }

    // ───────────────────────────── Public API ─────────────────────────────────

    // 카카오 유저 조회 후 없으면 신규 생성
    @Transactional
    public User findOrRegisterKakaoUser(
            String kakaoId, String email, String name, String profileUrl, DeviceType deviceType, String randomNickname
    ) {
        return userRepository.findByOauthIdAndOauthProvider(kakaoId, OAuthProvider.KAKAO)
                .orElseGet(() -> {
                    int initialGenerationCount = withdrawUserRepository.existsByOauthIdAndOauthProvider(
                            kakaoId, OAuthProvider.KAKAO
                    ) ? 0 : 3;
                    return userRepository.save(User.createKakaoUser(
                            kakaoId, email, name, profileUrl, deviceType, randomNickname, initialGenerationCount
                    ));
                });
    }

    // 테스트 유저 조회 후 없으면 신규 생성
    @Transactional
    public User findOrCreateTestUser(String testUserId, DeviceType deviceType,String randomNickname) {
        return userRepository.findByOauthIdAndOauthProvider(testUserId, OAuthProvider.TEST)
                .orElseGet(() -> userRepository.save(User.createTestUser(testUserId, deviceType, randomNickname)));
    }

    // 상대 유저 프로필 조회 — 첫 열람만 쿠폰 차감 + 열람 기록 upsert
    @Transactional
    public GetTargetUserProfileResponse getTargetUserProfile(Long viewerId, Long targetId) {
        if (viewerId.equals(targetId)) {
            throw new GeneralException(ErrorStatus.SELF_VIEW_NOT_ALLOWED);
        }

        // 프로필 등록 여부 검사
        User viewer = getUserOrThrow(viewerId);
        if (viewer.getProfileUrl() == null || viewer.getProfileUrl().isBlank()) {
            throw new GeneralException(ErrorStatus.USER_PROFILE_INCOMPLETE);
        }

        // 온보딩 완료 여부 검사
        User target = getUserOrThrow(targetId);
        if (target.getOnboardingStatus() != OnboardingStatus.COMPLETED) {
            throw new GeneralException(ErrorStatus.USER_ONBOARDING_INCOMPLETE);
        }

        int inserted = profileViewRepository.insertIgnoreProfileView(viewerId, targetId);
        if (inserted == 0) {
            profileViewRepository.updateViewedAt(viewerId, targetId);
            return GetTargetUserProfileResponse.from(target);
        }

        int decreased = userRepository.decreaseCouponCount(viewerId);
        if (decreased != 1) {
            throw new GeneralException(ErrorStatus.PROFILE_VIEW_COUPON_REQUIRED);
        }

        return GetTargetUserProfileResponse.from(target);
    }

    // 유저 프로필 조회 — Controller에 Entity 노출 방지
    @Transactional(readOnly = true)
    public GetUserProfileResponse getUserProfile(Long userId) {
        return GetUserProfileResponse.from(getUserOrThrow(userId));
    }

    // 온보딩 프로필 등록 — 중복 등록 방어 + appeals 이중 방어
    @Transactional
    public void registerUserOnboarding(Long userId, RegisterUserOnboardingRequest request) {
        User user = getUserOrThrow(userId);
        if (user.getOnboardingStatus() == OnboardingStatus.COMPLETED) {
            throw new GeneralException(ErrorStatus.ONBOARDING_ALREADY_COMPLETED);
        }
        if (request.appeals() == null || request.appeals().stream().anyMatch(a -> a == null || a.isBlank())) {
            throw new GeneralException(ErrorStatus.INVALID_APPEAL_CONTENT);
        }
        validateNickname(request.nickname(), userId);
        profanityFilter.validateAppeals(request.appeals());
        user.completeOnboarding(request.nickname(), request.mbti(), request.contact(), request.appeals(), request.gender());
    }

    // 닉네임 검증 — 비속어, 중복 여부 검사
    @Transactional(readOnly = true)
    public ValidateNicknameResponse validateNickname(Long userId, ValidateNicknameRequest request) {
        validateNickname(request.nickname(), userId);
        return ValidateNicknameResponse.ofAvailable();
    }

    // 유저 카드 리스트 조회 — 비인증 요청이면 전체 반환, 인증 요청이면 본인 제외
    @Transactional(readOnly = true)
    public List<GetUserCardResponse> getUserCardList(Long userId) {
        if (userId == null) {
            return userRepository.findAllByOnboardingStatusOrderByUpdatedAtDesc(OnboardingStatus.COMPLETED)
                    .stream()
                    .map(GetUserCardResponse::from)
                    .toList();
        }

        return userRepository.findAllByOnboardingStatusAndIdNotOrderByUpdatedAtDesc(
                        OnboardingStatus.COMPLETED, userId)
                .stream()
                .map(GetUserCardResponse::from)
                .toList();
    }

    // 내가 열람한 / 나를 열람한 유저 목록 통합 조회 — 최신순
    @Transactional(readOnly = true)
    public GetProfileViewListResponse getProfileViewList(Long userId) {
        User user = getUserOrThrow(userId);
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
        User user = getUserOrThrow(userId);
        if (user.getOnboardingStatus() != OnboardingStatus.COMPLETED) {
            throw new GeneralException(ErrorStatus.USER_ONBOARDING_INCOMPLETE);
        }
        validateNickname(request.nickname(), userId);
        profanityFilter.validateAppeals(request.appeals());
        user.updateProfile(request.toCommand());
    }

    // 결제용 전화번호 등록/수정
    @Transactional
    public void updatePhoneNumber(Long userId, UpdatePhoneNumberRequest request) {
        User user = getUserOrThrow(userId);
        user.updatePhoneNumber(request.phoneNumber());
    }

    // AuthService 전용 — logout/withdraw/reissue 시 사용
    @Transactional
    public User findByIdForUpdate(Long userId) {
        return getUserOrThrow(userId);
    }

    // 회원 탈퇴 시 관련 데이터를 정리하고 유저를 hard delete 합니다.
    public void withdraw(Long userId) {
        User user = getUserOrThrow(userId);
        saveWithdrawUser(user);
        aiImageService.deleteByUser(user);
        paymentService.deleteByUser(user);
        deleteProfileViews(user);
        adminCouponAdjustmentRepository.deleteAllByUser(user);
        bankDepositEventRepository.clearMatchedUser(user);
        userRepository.delete(user);
    }

    private void saveWithdrawUser(User user) {
        withdrawUserRepository.save(WithdrawUser.from(user));
    }

    // 유저 관련 열람 기록 삭제
    private void deleteProfileViews(User user) {
        profileViewRepository.deleteByViewerOrTarget(user, user);
    }

}

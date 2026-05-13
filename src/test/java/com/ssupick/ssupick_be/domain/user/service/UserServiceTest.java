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
import com.ssupick.ssupick_be.domain.user.entity.User;
import com.ssupick.ssupick_be.domain.user.entity.WithdrawUser;
import com.ssupick.ssupick_be.domain.user.enums.DeviceType;
import com.ssupick.ssupick_be.domain.user.enums.Gender;
import com.ssupick.ssupick_be.domain.user.enums.OAuthProvider;
import com.ssupick.ssupick_be.domain.user.enums.OnboardingStatus;
import com.ssupick.ssupick_be.domain.user.repository.ProfileViewRepository;
import com.ssupick.ssupick_be.domain.user.repository.UserRepository;
import com.ssupick.ssupick_be.domain.user.repository.WithdrawUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private AiImageService aiImageService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileViewRepository profileViewRepository;

    @Mock
    private ProfanityFilter profanityFilter;

    @Mock
    private PaymentService paymentService;

    @Mock
    private AdminCouponAdjustmentRepository adminCouponAdjustmentRepository;

    @Mock
    private BankDepositEventRepository bankDepositEventRepository;

    @Mock
    private WithdrawUserRepository withdrawUserRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        lenient().when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(withdrawUserRepository.save(any(WithdrawUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    // 온보딩 시 닉네임에 비속어가 있으면 예외를 던지고 온보딩 상태가 변경되지 않습니다.
    @Test
    void registerUserOnboarding_throwsWhenNicknameProfanityDetected() {
        User user = User.createTestUser("test-user", DeviceType.IOS);
        RegisterUserOnboardingRequest request = new RegisterUserOnboardingRequest(
                "bad-nickname", "INTJ", List.of("청순"), "@ssupick", Gender.FEMALE
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doThrow(new GeneralException(ErrorStatus.NICKNAME_PROFANITY_DETECTED))
                .when(profanityFilter).validateNickname("bad-nickname");

        assertThatThrownBy(() -> userService.registerUserOnboarding(1L, request))
                .isInstanceOfSatisfying(GeneralException.class, e ->
                        assertThat(e.getErrorStatus()).isEqualTo(ErrorStatus.NICKNAME_PROFANITY_DETECTED));

        assertThat(user.getOnboardingStatus()).isEqualTo(OnboardingStatus.INCOMPLETE);
        verify(profanityFilter).validateNickname("bad-nickname");
    }

    // 온보딩 시 어필 항목에 비속어가 있으면 예외를 던지고 온보딩 상태가 변경되지 않습니다.
    @Test
    void registerUserOnboarding_throwsWhenAppealProfanityDetected() {
        User user = User.createTestUser("test-user", DeviceType.IOS);
        RegisterUserOnboardingRequest request = new RegisterUserOnboardingRequest(
                "nickname", "INTJ", List.of("bad-appeal"), "@ssupick", Gender.FEMALE
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doThrow(new GeneralException(ErrorStatus.APPEAL_PROFANITY_DETECTED))
                .when(profanityFilter).validateAppeals(List.of("bad-appeal"));

        assertThatThrownBy(() -> userService.registerUserOnboarding(1L, request))
                .isInstanceOfSatisfying(GeneralException.class, e ->
                        assertThat(e.getErrorStatus()).isEqualTo(ErrorStatus.APPEAL_PROFANITY_DETECTED));

        assertThat(user.getOnboardingStatus()).isEqualTo(OnboardingStatus.INCOMPLETE);
        verify(profanityFilter).validateAppeals(List.of("bad-appeal"));
    }

    // 프로필 수정 시 닉네임에 비속어가 있으면 예외를 던지고 닉네임이 변경되지 않습니다.
    @Test
    void updateUserProfile_throwsWhenNicknameProfanityDetected() {
        User user = User.createTestUser("test-user", DeviceType.IOS);
        user.completeOnboarding("old", "INTJ", "@old", List.of("청순"), Gender.FEMALE);
        UpdateUserProfileRequest request = new UpdateUserProfileRequest(
                "bad-nickname", "ENTP", List.of("귀여움"), "@new"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doThrow(new GeneralException(ErrorStatus.NICKNAME_PROFANITY_DETECTED))
                .when(profanityFilter).validateNickname("bad-nickname");

        assertThatThrownBy(() -> userService.updateUserProfile(1L, request))
                .isInstanceOfSatisfying(GeneralException.class, e ->
                        assertThat(e.getErrorStatus()).isEqualTo(ErrorStatus.NICKNAME_PROFANITY_DETECTED));

        assertThat(user.getNickname()).isEqualTo("old");
        verify(profanityFilter).validateNickname("bad-nickname");
    }

    // 프로필 수정 시 어필 항목에 비속어가 있으면 예외를 던지고 닉네임이 변경되지 않습니다.
    @Test
    void updateUserProfile_throwsWhenAppealProfanityDetected() {
        User user = User.createTestUser("test-user", DeviceType.IOS);
        user.completeOnboarding("old", "INTJ", "@old", List.of("청순"), Gender.FEMALE);
        UpdateUserProfileRequest request = new UpdateUserProfileRequest(
                "nickname", "ENTP", List.of("bad-appeal"), "@new"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doThrow(new GeneralException(ErrorStatus.APPEAL_PROFANITY_DETECTED))
                .when(profanityFilter).validateAppeals(List.of("bad-appeal"));

        assertThatThrownBy(() -> userService.updateUserProfile(1L, request))
                .isInstanceOfSatisfying(GeneralException.class, e ->
                        assertThat(e.getErrorStatus()).isEqualTo(ErrorStatus.APPEAL_PROFANITY_DETECTED));

        assertThat(user.getNickname()).isEqualTo("old");
        verify(profanityFilter).validateAppeals(List.of("bad-appeal"));
    }

    // 결제용 전화번호를 유저 엔티티에 저장합니다.
    @Test
    void updatePhoneNumber_updatesUserPhoneNumber() {
        User user = User.createTestUser("test-user", DeviceType.IOS);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.updatePhoneNumber(1L, new UpdatePhoneNumberRequest("01012345678"));

        assertThat(user.getPhoneNumber()).isEqualTo("01012345678");
    }

    // 탈퇴 이력이 없는 카카오 신규 유저는 기본 이미지 생성 횟수 3회로 생성됩니다.
    @Test
    void findOrRegisterKakaoUser_createsNewUserWithDefaultGenerationCountWhenNotWithdrawn() {
        when(userRepository.findByOauthIdAndOauthProvider("12345", OAuthProvider.KAKAO))
                .thenReturn(Optional.empty());
        when(withdrawUserRepository.existsByOauthIdAndOauthProvider("12345", OAuthProvider.KAKAO))
                .thenReturn(false);

        User user = userService.findOrRegisterKakaoUser(
                "12345", "test@test.com", "테스트", "https://profile.jpg", DeviceType.IOS, "랜덤닉네임"
        );

        assertThat(user.getRemainingGenerationCount()).isEqualTo(3);
    }

    // 탈퇴 이력이 있는 카카오 재가입 유저는 이미지 생성 횟수 0회로 생성됩니다.
    @Test
    void findOrRegisterKakaoUser_createsRejoinedUserWithZeroGenerationCountWhenWithdrawn() {
        when(userRepository.findByOauthIdAndOauthProvider("12345", OAuthProvider.KAKAO))
                .thenReturn(Optional.empty());
        when(withdrawUserRepository.existsByOauthIdAndOauthProvider("12345", OAuthProvider.KAKAO))
                .thenReturn(true);

        User user = userService.findOrRegisterKakaoUser(
                "12345", "test@test.com", "테스트", "https://profile.jpg", DeviceType.IOS, "랜덤닉네임"
        );

        assertThat(user.getRemainingGenerationCount()).isZero();
    }

    // 회원 탈퇴 시 hard delete 전에 withdraw_user에 소셜 계정 이력을 저장합니다.
    @Test
    void withdraw_savesWithdrawUserBeforeHardDelete() {
        User user = User.createKakaoUser(
                "12345", "test@test.com", "테스트", "https://profile.jpg", DeviceType.IOS, "랜덤닉네임"
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.withdraw(1L);

        ArgumentCaptor<WithdrawUser> captor = ArgumentCaptor.forClass(WithdrawUser.class);
        verify(withdrawUserRepository).save(captor.capture());
        assertThat(captor.getValue().getOauthId()).isEqualTo("12345");
        assertThat(captor.getValue().getOauthProvider()).isEqualTo(OAuthProvider.KAKAO);
        verify(userRepository).delete(user);
    }
}

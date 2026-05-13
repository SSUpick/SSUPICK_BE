package com.ssupick.ssupick_be.domain.admin.service;

import com.ssupick.ssupick_be.common.discord.DiscordNotificationService;
import com.ssupick.ssupick_be.common.exception.GeneralException;
import com.ssupick.ssupick_be.common.jwt.JwtService;
import com.ssupick.ssupick_be.common.status.ErrorStatus;
import com.ssupick.ssupick_be.domain.admin.dto.request.AdminGenerationCountSetRequest;
import com.ssupick.ssupick_be.domain.admin.dto.response.AdminGenerationCountSetResponse;
import com.ssupick.ssupick_be.domain.admin.properties.AdminProperties;
import com.ssupick.ssupick_be.domain.admin.repository.AdminCouponAdjustmentRepository;
import com.ssupick.ssupick_be.domain.user.entity.User;
import com.ssupick.ssupick_be.domain.user.enums.DeviceType;
import com.ssupick.ssupick_be.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AdminProperties adminProperties;

    @Mock
    private JwtService jwtService;

    @Mock
    private AdminLoginAttemptService adminLoginAttemptService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdminCouponAdjustmentRepository adminCouponAdjustmentRepository;

    @Mock
    private DiscordNotificationService discordNotificationService;

    @InjectMocks
    private AdminService adminService;

    @Test
    void setGenerationCount_updatesUserGenerationCountByNickname() {
        User user = User.createTestUser("test-user", DeviceType.IOS, "푸른하늘");
        when(userRepository.findByNickname("푸른하늘")).thenReturn(Optional.of(user));

        AdminGenerationCountSetResponse response = adminService.setGenerationCount(
                new AdminGenerationCountSetRequest(" 푸른하늘 ", 5)
        );

        assertThat(user.getRemainingGenerationCount()).isEqualTo(5);
        assertThat(response.nickname()).isEqualTo("푸른하늘");
        assertThat(response.remainingGenerationCount()).isEqualTo(5);
        verify(userRepository).findByNickname("푸른하늘");
    }

    @Test
    void setGenerationCount_throwsWhenNicknameNotFound() {
        when(userRepository.findByNickname("없는닉네임")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.setGenerationCount(
                new AdminGenerationCountSetRequest("없는닉네임", 1)
        ))
                .isInstanceOfSatisfying(GeneralException.class, e ->
                        assertThat(e.getErrorStatus()).isEqualTo(ErrorStatus.USER_NOT_FOUND));
    }
}

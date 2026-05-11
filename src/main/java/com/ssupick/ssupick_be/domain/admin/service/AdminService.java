package com.ssupick.ssupick_be.domain.admin.service;

import com.ssupick.ssupick_be.common.exception.GeneralException;
import com.ssupick.ssupick_be.common.jwt.JwtService;
import com.ssupick.ssupick_be.common.status.ErrorStatus;
import com.ssupick.ssupick_be.domain.admin.dto.request.AdminLoginRequest;
import com.ssupick.ssupick_be.domain.admin.dto.response.AdminLoginResponse;
import com.ssupick.ssupick_be.domain.admin.properties.AdminProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminProperties adminProperties;
    private final JwtService jwtService;
    private final AdminLoginAttemptService adminLoginAttemptService;

    public AdminLoginResponse login(String clientIp, AdminLoginRequest request) {
        if (adminLoginAttemptService.isLocked(clientIp)) {
            throw new GeneralException(ErrorStatus.TOO_MANY_REQUESTS);
        }

        if (!matches(adminProperties.username(), request.username())
                || !matches(adminProperties.password(), request.password())) {
            adminLoginAttemptService.recordFailure(clientIp);
            throw new GeneralException(ErrorStatus.UNAUTHORIZED);
        }

        adminLoginAttemptService.recordSuccess(clientIp);
        return new AdminLoginResponse(jwtService.generateAdminAccessToken());
    }

    private boolean matches(String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }

        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8)
        );
    }
}

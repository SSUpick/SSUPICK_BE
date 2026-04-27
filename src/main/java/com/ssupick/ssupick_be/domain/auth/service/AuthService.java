package com.ssupick.ssupick_be.domain.auth.service;

import com.ssupick.ssupick_be.common.exception.GeneralException;
import com.ssupick.ssupick_be.common.jwt.JwtService;
import com.ssupick.ssupick_be.common.jwt.TokenIssuance;
import com.ssupick.ssupick_be.common.status.ErrorStatus;
import com.ssupick.ssupick_be.domain.auth.dto.request.LoginRequest;
import com.ssupick.ssupick_be.domain.auth.dto.response.LoginResponse;
import com.ssupick.ssupick_be.domain.auth.dto.response.ReissueResponse;
import com.ssupick.ssupick_be.domain.user.entity.User;
import com.ssupick.ssupick_be.domain.user.service.UserService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;

    @Value("${test.secret-key}")
    private String testSecretKey;

    // 시크릿 키 검증 후 테스트 유저 조회/생성 및 JWT 발급
    @Transactional
    public LoginResponse login(String secretKey, LoginRequest request) {
        if (!MessageDigest.isEqual(
                testSecretKey.getBytes(StandardCharsets.UTF_8),
                secretKey.getBytes(StandardCharsets.UTF_8))) {
            throw new GeneralException(ErrorStatus.FORBIDDEN);
        }

        User user = userService.findOrCreateTestUser(request.testUserId(), request.deviceType());
        TokenIssuance tokens = jwtService.issueTokens(user);

        return new LoginResponse(user.getId(), tokens.accessToken(), tokens.refreshToken());
    }

    @Transactional
    public ReissueResponse reissue(String refreshToken) {
        // 1차 검증: 서명 및 만료 확인
        Claims claims = jwtService.validateRefreshTokenSignature(refreshToken);
        Long userId = Long.parseLong(claims.getSubject());

        // dirty checking 보장을 위해 쓰기용 메서드로 조회
        User user = userService.findByIdForUpdate(userId);

        // 2차 검증: DB 저장 해시값과 비교
        if (user.getRefreshToken() == null)
            throw new GeneralException(ErrorStatus.REFRESH_TOKEN_NOT_FOUND);
        jwtService.validateRefreshToken(refreshToken, user.getRefreshToken());

        // 토큰 재발급 (issueTokens 내부에서 refresh token 해시 저장)
        TokenIssuance tokens = jwtService.issueTokens(user);
        return new ReissueResponse(tokens.accessToken(), tokens.refreshToken());
    }

}

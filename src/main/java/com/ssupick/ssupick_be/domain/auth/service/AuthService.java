package com.ssupick.ssupick_be.domain.auth.service;

import com.ssupick.ssupick_be.common.exception.GeneralException;
import com.ssupick.ssupick_be.common.jwt.JwtService;
import com.ssupick.ssupick_be.common.jwt.TokenIssuance;
import com.ssupick.ssupick_be.common.status.ErrorStatus;
import com.ssupick.ssupick_be.domain.auth.dto.ReissueResponse;
import com.ssupick.ssupick_be.domain.user.entity.User;
import com.ssupick.ssupick_be.domain.user.service.UserService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;

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

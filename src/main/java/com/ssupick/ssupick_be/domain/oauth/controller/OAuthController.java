package com.ssupick.ssupick_be.domain.oauth.controller;

import com.ssupick.ssupick_be.common.response.ApiResponse;
import com.ssupick.ssupick_be.common.status.SuccessStatus;
import com.ssupick.ssupick_be.domain.oauth.controller.docs.OAuthControllerDocs;
import com.ssupick.ssupick_be.domain.oauth.dto.request.OAuthKakaoLoginRequest;
import com.ssupick.ssupick_be.domain.oauth.dto.response.OAuthLoginResponse;
import com.ssupick.ssupick_be.domain.oauth.service.OAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/oauth")
@RequiredArgsConstructor
public class OAuthController implements OAuthControllerDocs {

    private final OAuthService oAuthService;

    @PostMapping("/kakao/login")
    @Override
    public ResponseEntity<ApiResponse<OAuthLoginResponse>> kakaoLogin(
            @Valid @RequestBody OAuthKakaoLoginRequest request
    ) {
        OAuthLoginResponse response = oAuthService.kakaoLogin(request);
        return ApiResponse.success(SuccessStatus.OAUTH_KAKAO_LOGIN_SUCCESS, response);
    }
}

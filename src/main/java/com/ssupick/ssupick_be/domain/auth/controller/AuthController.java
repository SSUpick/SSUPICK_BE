package com.ssupick.ssupick_be.domain.auth.controller;

import com.ssupick.ssupick_be.common.response.ApiResponse;
import com.ssupick.ssupick_be.common.status.SuccessStatus;
import com.ssupick.ssupick_be.domain.auth.controller.docs.AuthControllerDocs;
import com.ssupick.ssupick_be.domain.auth.dto.request.LoginRequest;
import com.ssupick.ssupick_be.domain.auth.dto.response.LoginResponse;
import com.ssupick.ssupick_be.domain.auth.dto.response.ReissueResponse;
import com.ssupick.ssupick_be.domain.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerDocs {

    private final AuthService authService;

    @PostMapping("/test/login")
    @Override
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @RequestHeader("Test-Secret-Key") String secretKey,
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response = authService.login(secretKey, request);
        return ApiResponse.success(SuccessStatus.LOGIN_SUCCESS, response);
    }

    @PostMapping("/token/reissue")
    @Override
    public ResponseEntity<ApiResponse<ReissueResponse>> reissue(
            @RequestHeader("Refresh-Token") String refreshToken
    ) {
        ReissueResponse response = authService.reissue(refreshToken);
        return ApiResponse.success(SuccessStatus.REISSUE_SUCCESS, response);
    }

    @PostMapping("/logout")
    @Override
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal Long userId
    ) {
        authService.logout(userId);
        return ApiResponse.success(SuccessStatus.LOGOUT_SUCCESS);
    }

    @DeleteMapping("/withdraw")
    @Override
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @AuthenticationPrincipal Long userId
    ) {
        authService.withdraw(userId);
        return ApiResponse.success(SuccessStatus.WITHDRAW_SUCCESS);
    }
}

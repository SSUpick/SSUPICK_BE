package com.ssupick.ssupick_be.domain.auth.controller;

import com.ssupick.ssupick_be.common.response.ApiResponse;
import com.ssupick.ssupick_be.common.status.SuccessStatus;
import com.ssupick.ssupick_be.domain.auth.controller.docs.AuthControllerDocs;
import com.ssupick.ssupick_be.domain.auth.dto.ReissueResponse;
import com.ssupick.ssupick_be.domain.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerDocs {

    private final AuthService authService;

    @PostMapping("/token/reissue")
    @Override
    public ResponseEntity<ApiResponse<ReissueResponse>> reissue(
            @RequestHeader("Refresh-Token") String refreshToken
    ) {
        ReissueResponse response = authService.reissue(refreshToken);
        return ApiResponse.success(SuccessStatus.REISSUE_SUCCESS, response);
    }
}

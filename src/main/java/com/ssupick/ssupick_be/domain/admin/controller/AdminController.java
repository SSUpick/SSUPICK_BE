package com.ssupick.ssupick_be.domain.admin.controller;

import com.ssupick.ssupick_be.common.response.ApiResponse;
import com.ssupick.ssupick_be.common.status.SuccessStatus;
import com.ssupick.ssupick_be.domain.admin.controller.docs.AdminControllerDocs;
import com.ssupick.ssupick_be.domain.admin.dto.request.AdminLoginRequest;
import com.ssupick.ssupick_be.domain.admin.dto.response.AdminLoginResponse;
import com.ssupick.ssupick_be.domain.admin.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController implements AdminControllerDocs {

    private final AdminService adminService;

    @PostMapping("/login")
    @Override
    public ResponseEntity<ApiResponse<AdminLoginResponse>> login(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody AdminLoginRequest request
    ) {
        AdminLoginResponse response = adminService.login(resolveClientIp(httpServletRequest), request);
        return ApiResponse.success(SuccessStatus.LOGIN_SUCCESS, response);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}

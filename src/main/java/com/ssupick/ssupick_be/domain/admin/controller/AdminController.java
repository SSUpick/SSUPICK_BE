package com.ssupick.ssupick_be.domain.admin.controller;

import com.ssupick.ssupick_be.common.response.ApiResponse;
import com.ssupick.ssupick_be.common.status.SuccessStatus;
import com.ssupick.ssupick_be.domain.admin.controller.docs.AdminControllerDocs;
import com.ssupick.ssupick_be.domain.admin.dto.request.AdminCouponAddRequest;
import com.ssupick.ssupick_be.domain.admin.dto.request.AdminLoginRequest;
import com.ssupick.ssupick_be.domain.admin.dto.response.AdminCouponAddResponse;
import com.ssupick.ssupick_be.domain.admin.dto.response.AdminLoginResponse;
import com.ssupick.ssupick_be.domain.admin.dto.response.AdminUserSearchResponse;
import com.ssupick.ssupick_be.domain.admin.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/users")
    @Override
    public ResponseEntity<ApiResponse<List<AdminUserSearchResponse>>> searchUsers(
            @RequestParam(required = false) String keyword
    ) {
        List<AdminUserSearchResponse> response = adminService.searchUsers(keyword);
        return ApiResponse.success(SuccessStatus.ADMIN_USER_SEARCH_SUCCESS, response);
    }

    @PostMapping("/coupons/add")
    @Override
    public ResponseEntity<ApiResponse<AdminCouponAddResponse>> addCoupon(
            @Valid @RequestBody AdminCouponAddRequest request
    ) {
        AdminCouponAddResponse response = adminService.addCoupon(request);
        return ApiResponse.success(SuccessStatus.ADMIN_COUPON_ADD_SUCCESS, response);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}

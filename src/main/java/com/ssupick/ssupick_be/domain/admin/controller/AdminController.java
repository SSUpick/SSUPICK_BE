package com.ssupick.ssupick_be.domain.admin.controller;

import com.ssupick.ssupick_be.common.response.ApiResponse;
import com.ssupick.ssupick_be.common.status.SuccessStatus;
import com.ssupick.ssupick_be.domain.admin.controller.docs.AdminControllerDocs;
import com.ssupick.ssupick_be.domain.admin.dto.request.AdminCouponAddRequest;
import com.ssupick.ssupick_be.domain.admin.dto.request.AdminGenerationCountSetRequest;
import com.ssupick.ssupick_be.domain.admin.dto.request.AdminLoginRequest;
import com.ssupick.ssupick_be.domain.admin.dto.response.AdminCouponAddResponse;
import com.ssupick.ssupick_be.domain.admin.dto.response.AdminGenerationCountSetResponse;
import com.ssupick.ssupick_be.domain.admin.dto.response.AdminLoginResponse;
import com.ssupick.ssupick_be.domain.admin.dto.response.AdminUserSearchResponse;
import com.ssupick.ssupick_be.domain.admin.service.AdminService;
import com.ssupick.ssupick_be.domain.bank.dto.request.BankDepositAssignRequest;
import com.ssupick.ssupick_be.domain.bank.dto.response.BankDepositEventResponse;
import com.ssupick.ssupick_be.domain.bank.service.BankDepositService;
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
    private final BankDepositService bankDepositService;

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

    @PatchMapping("/generation-count")
    @Override
    public ResponseEntity<ApiResponse<AdminGenerationCountSetResponse>> setGenerationCount(
            @Valid @RequestBody AdminGenerationCountSetRequest request
    ) {
        AdminGenerationCountSetResponse response = adminService.setGenerationCount(request);
        return ApiResponse.success(SuccessStatus.ADMIN_GENERATION_COUNT_SET_SUCCESS, response);
    }

    @GetMapping("/deposit-events")
    @Override
    public ResponseEntity<ApiResponse<List<BankDepositEventResponse>>> getDepositEvents() {
        List<BankDepositEventResponse> response = bankDepositService.getPendingEvents();
        return ApiResponse.success(SuccessStatus.ADMIN_DEPOSIT_EVENT_LIST_SUCCESS, response);
    }

    @PostMapping("/deposit-events/{eventId}/assign")
    @Override
    public ResponseEntity<ApiResponse<BankDepositEventResponse>> assignDepositEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody BankDepositAssignRequest request
    ) {
        BankDepositEventResponse response = bankDepositService.assign(eventId, request);
        return ApiResponse.success(SuccessStatus.ADMIN_DEPOSIT_EVENT_ASSIGN_SUCCESS, response);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}

package com.ssupick.ssupick_be.domain.admin.service;

import com.ssupick.ssupick_be.common.exception.GeneralException;
import com.ssupick.ssupick_be.common.jwt.JwtService;
import com.ssupick.ssupick_be.common.status.ErrorStatus;
import com.ssupick.ssupick_be.domain.admin.dto.request.AdminCouponAddRequest;
import com.ssupick.ssupick_be.domain.admin.dto.request.AdminLoginRequest;
import com.ssupick.ssupick_be.domain.admin.dto.response.AdminCouponAddResponse;
import com.ssupick.ssupick_be.domain.admin.dto.response.AdminLoginResponse;
import com.ssupick.ssupick_be.domain.admin.dto.response.AdminUserSearchResponse;
import com.ssupick.ssupick_be.domain.admin.entity.AdminCouponAdjustment;
import com.ssupick.ssupick_be.domain.admin.properties.AdminProperties;
import com.ssupick.ssupick_be.domain.admin.repository.AdminCouponAdjustmentRepository;
import com.ssupick.ssupick_be.domain.payment.enums.CouponProduct;
import com.ssupick.ssupick_be.domain.user.entity.User;
import com.ssupick.ssupick_be.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminProperties adminProperties;
    private final JwtService jwtService;
    private final AdminLoginAttemptService adminLoginAttemptService;
    private final UserRepository userRepository;
    private final AdminCouponAdjustmentRepository adminCouponAdjustmentRepository;

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

    @Transactional(readOnly = true)
    public List<AdminUserSearchResponse> searchUsers(String keyword) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        Long userId = parseUserId(normalizedKeyword);

        return userRepository.searchForAdmin(normalizedKeyword, userId)
                .stream()
                .limit(30)
                .map(AdminUserSearchResponse::from)
                .toList();
    }

    @Transactional
    public AdminCouponAddResponse addCoupon(AdminCouponAddRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
        CouponProduct couponProduct = request.couponProduct();

        user.increaseCouponCount(couponProduct.getCouponCount());

        AdminCouponAdjustment adjustment = AdminCouponAdjustment.create(
                user,
                couponProduct.getCouponCount(),
                user.getRemainingCouponCount(),
                couponProduct.name(),
                null,
                couponProduct.getPrice(),
                "ADMIN_MANUAL_COUPON_CHARGE"
        );

        AdminCouponAdjustment savedAdjustment = adminCouponAdjustmentRepository.save(adjustment);
        return AdminCouponAddResponse.from(savedAdjustment);
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

    private Long parseUserId(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(keyword);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

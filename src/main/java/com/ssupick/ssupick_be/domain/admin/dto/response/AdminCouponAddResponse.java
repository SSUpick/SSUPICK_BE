package com.ssupick.ssupick_be.domain.admin.dto.response;

import com.ssupick.ssupick_be.domain.admin.entity.AdminCouponAdjustment;

public record AdminCouponAddResponse(
        Long adjustmentId,
        Long userId,
        int chargedCouponCount,
        int remainingCouponCount
) {
    public static AdminCouponAddResponse from(AdminCouponAdjustment adjustment) {
        return new AdminCouponAddResponse(
                adjustment.getId(),
                adjustment.getUser().getId(),
                adjustment.getDeltaCount(),
                adjustment.getRemainingCouponCount()
        );
    }
}

package com.ssupick.ssupick_be.domain.admin.dto.request;

import com.ssupick.ssupick_be.domain.payment.enums.CouponProduct;
import jakarta.validation.constraints.NotNull;

public record AdminCouponAddRequest(

        @NotNull(message = "유저 ID는 필수입니다.")
        Long userId,

        @NotNull(message = "쿠폰 상품은 필수입니다.")
        CouponProduct couponProduct
) {}

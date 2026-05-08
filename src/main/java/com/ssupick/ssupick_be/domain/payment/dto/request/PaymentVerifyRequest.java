package com.ssupick.ssupick_be.domain.payment.dto.request;

import com.ssupick.ssupick_be.domain.payment.enums.CouponProduct;
import jakarta.validation.constraints.NotNull;

public record PaymentVerifyRequest(
        @NotNull(message = "쿠폰 상품은 필수입니다.")
        CouponProduct couponProduct
) {}

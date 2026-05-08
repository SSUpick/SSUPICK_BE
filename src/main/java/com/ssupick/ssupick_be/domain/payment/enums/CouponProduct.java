package com.ssupick.ssupick_be.domain.payment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CouponProduct {

    COUPON_1(1, 1000L, "프로필 조회 쿠폰 1개"),
    COUPON_4(4, 3000L, "프로필 조회 쿠폰 4개"),
    COUPON_8(8, 5000L, "프로필 조회 쿠폰 8개");

    private final int couponCount;
    private final Long price;
    private final String orderName;
}

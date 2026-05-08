package com.ssupick.ssupick_be.domain.payment.dto.response;

import com.ssupick.ssupick_be.domain.payment.enums.CouponProduct;

public record CouponProductResponse(
        String productCode,
        String orderName,
        int couponCount,
        Long price
) {
    // 쿠폰 상품 enum을 프론트 응답 DTO로 변환합니다.
    public static CouponProductResponse from(CouponProduct product) {
        return new CouponProductResponse(
                product.name(),
                product.getOrderName(),
                product.getCouponCount(),
                product.getPrice()
        );
    }
}

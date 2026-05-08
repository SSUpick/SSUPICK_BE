package com.ssupick.ssupick_be.domain.payment.dto.response;

public record PaymentVerifyResponse(
        String paymentId,
        String status,
        Long totalAmount,
        int chargedCouponCount,
        int remainingCouponCount
) {
    // PortOne 결제 조회 결과와 쿠폰 충전 결과를 결제 검증 응답으로 변환합니다.
    public static PaymentVerifyResponse from(
            PortOnePaymentResponse payment,
            int chargedCouponCount,
            int remainingCouponCount
    ) {
        Long totalAmount = payment.amount() != null ? payment.amount().total() : null;
        return new PaymentVerifyResponse(
                payment.id(),
                payment.status(),
                totalAmount,
                chargedCouponCount,
                remainingCouponCount
        );
    }
}

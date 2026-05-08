package com.ssupick.ssupick_be.domain.payment.dto.response;

public record PaymentVerifyResponse(
        String paymentId,
        String status,
        Long totalAmount
) {
    public static PaymentVerifyResponse from(PortOnePaymentResponse payment) {
        Long totalAmount = payment.amount() != null ? payment.amount().total() : null;
        return new PaymentVerifyResponse(payment.id(), payment.status(), totalAmount);
    }
}

package com.ssupick.ssupick_be.domain.payment.service;

import com.ssupick.ssupick_be.common.exception.GeneralException;
import com.ssupick.ssupick_be.common.status.ErrorStatus;
import com.ssupick.ssupick_be.domain.payment.dto.response.PaymentVerifyResponse;
import com.ssupick.ssupick_be.domain.payment.dto.response.PortOnePaymentResponse;
import com.ssupick.ssupick_be.domain.payment.entity.Payment;
import com.ssupick.ssupick_be.domain.payment.enums.CouponProduct;
import com.ssupick.ssupick_be.domain.payment.enums.PaymentStatus;
import com.ssupick.ssupick_be.domain.payment.repository.PaymentRepository;
import com.ssupick.ssupick_be.domain.user.entity.User;
import com.ssupick.ssupick_be.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PaymentWriter {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    // 외부 API 호출 없이 READY 결제 기록을 PAID로 전환하고 쿠폰을 충전하는 짧은 트랜잭션입니다.
    @Transactional
    public PaymentVerifyResponse completePayment(
            Long userId,
            String paymentId,
            CouponProduct couponProduct,
            PortOnePaymentResponse payment
    ) {
        Long actualAmount = payment.amount() != null ? payment.amount().total() : null;

        int markedPaid = paymentRepository.markReadyPaymentAsPaid(
                paymentId,
                userId,
                couponProduct,
                actualAmount,
                couponProduct.getCouponCount(),
                PaymentStatus.READY,
                PaymentStatus.PAID
        );

        // 중복 요청 또는 잘못된 요청 — 기존 결제 상태를 확인합니다.
        if (markedPaid == 0) {
            Payment existingPayment = paymentRepository.findByPaymentIdAndUserId(paymentId, userId)
                    .orElseThrow(() -> new GeneralException(ErrorStatus.PAYMENT_ALREADY_PROCESSED));
            if (existingPayment.getStatus() != PaymentStatus.PAID) {
                throw new GeneralException(ErrorStatus.PAYMENT_STATUS_INVALID);
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

            return new PaymentVerifyResponse(
                    existingPayment.getPaymentId(),
                    existingPayment.getStatus().name(),
                    existingPayment.getPaidAmount(),
                    0,
                    user.getRemainingCouponCount()
            );
        }

        // 신규 완료 요청 — PAID 전환에 성공한 요청만 쿠폰을 충전합니다.
        int updated = userRepository.increaseCouponCount(userId, couponProduct.getCouponCount());
        if (updated != 1) {
            throw new GeneralException(ErrorStatus.USER_NOT_FOUND);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        return PaymentVerifyResponse.from(
                payment,
                couponProduct.getCouponCount(),
                user.getRemainingCouponCount()
        );
    }
}

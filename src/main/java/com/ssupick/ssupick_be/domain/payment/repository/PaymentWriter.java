package com.ssupick.ssupick_be.domain.payment.repository;

import com.ssupick.ssupick_be.common.exception.GeneralException;
import com.ssupick.ssupick_be.common.status.ErrorStatus;
import com.ssupick.ssupick_be.domain.payment.dto.response.PaymentVerifyResponse;
import com.ssupick.ssupick_be.domain.payment.dto.response.PortOnePaymentResponse;
import com.ssupick.ssupick_be.domain.payment.entity.Payment;
import com.ssupick.ssupick_be.domain.payment.enums.CouponProduct;
import com.ssupick.ssupick_be.domain.user.entity.User;
import com.ssupick.ssupick_be.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PaymentWriter {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    // DB 반영만 담당하는 짧은 트랜잭션 — 외부 API 호출 없이 저장 + 쿠폰 충전만 수행합니다.
    @Transactional
    public PaymentVerifyResponse completePayment(
            Long userId,
            String paymentId,
            CouponProduct couponProduct,
            PortOnePaymentResponse payment
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));

        Payment existingPayment = paymentRepository.findByPaymentId(paymentId).orElse(null);
        if (existingPayment != null) {
            if (!existingPayment.getUser().getId().equals(userId)) {
                throw new GeneralException(ErrorStatus.PAYMENT_ALREADY_PROCESSED);
            }
            return new PaymentVerifyResponse(
                    existingPayment.getPaymentId(),
                    existingPayment.getStatus().name(),
                    existingPayment.getPaidAmount(),
                    0,
                    user.getRemainingCouponCount()
            );
        }

        Long actualAmount = payment.amount() != null ? payment.amount().total() : null;

        // 1. 결제 기록을 먼저 저장합니다. paymentId 중복 시 PAYMENT_ALREADY_PROCESSED 예외가 발생합니다.
        try {
            paymentRepository.saveAndFlush(Payment.paid(paymentId, user, couponProduct, actualAmount));
        } catch (DataIntegrityViolationException e) {
            throw new GeneralException(ErrorStatus.PAYMENT_ALREADY_PROCESSED);
        }

        // 2. 저장에 성공한 요청만 쿠폰을 충전합니다.
        user.increaseCouponCount(couponProduct.getCouponCount());

        return PaymentVerifyResponse.from(
                payment,
                couponProduct.getCouponCount(),
                user.getRemainingCouponCount()
        );
    }
}

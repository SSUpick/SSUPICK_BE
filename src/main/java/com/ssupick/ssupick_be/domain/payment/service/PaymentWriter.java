package com.ssupick.ssupick_be.domain.payment.service;

import com.ssupick.ssupick_be.common.exception.GeneralException;
import com.ssupick.ssupick_be.common.status.ErrorStatus;
import com.ssupick.ssupick_be.domain.payment.dto.response.PaymentVerifyResponse;
import com.ssupick.ssupick_be.domain.payment.dto.response.PortOnePaymentResponse;
import com.ssupick.ssupick_be.domain.payment.entity.Payment;
import com.ssupick.ssupick_be.domain.payment.enums.CouponProduct;
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

    // 외부 API 호출 없이 DB 저장 + 쿠폰 충전만 수행하는 짧은 트랜잭션입니다.
    // INSERT IGNORE로 중복 요청을 예외 없이 처리하고, affected row로 신규/중복 여부를 판단합니다.
    @Transactional
    public PaymentVerifyResponse completePayment(
            Long userId,
            String paymentId,
            CouponProduct couponProduct,
            PortOnePaymentResponse payment
    ) {
        Long actualAmount = payment.amount() != null ? payment.amount().total() : null;

        int inserted = paymentRepository.insertIgnorePaidPayment(
                paymentId,
                userId,
                couponProduct.name(),
                actualAmount,
                couponProduct.getCouponCount()
        );

        // 중복 요청 — 소유자 검증 후 기존 결제 결과를 반환합니다.
        if (inserted == 0) {
            // paymentId + userId로 조회 — 없으면 다른 유저의 결제이므로 거부합니다.
            Payment existingPayment = paymentRepository.findByPaymentIdAndUserId(paymentId, userId)
                    .orElseThrow(() -> new GeneralException(ErrorStatus.PAYMENT_ALREADY_PROCESSED));

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

        // 신규 요청 — 쿠폰을 충전하고 결과를 반환합니다.
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

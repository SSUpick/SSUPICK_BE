package com.ssupick.ssupick_be.domain.payment.service;

import com.ssupick.ssupick_be.common.exception.GeneralException;
import com.ssupick.ssupick_be.common.status.ErrorStatus;
import com.ssupick.ssupick_be.domain.payment.client.PortOneClient;
import com.ssupick.ssupick_be.domain.payment.dto.request.PaymentVerifyRequest;
import com.ssupick.ssupick_be.domain.payment.dto.response.CouponProductResponse;
import com.ssupick.ssupick_be.domain.payment.dto.response.PaymentVerifyResponse;
import com.ssupick.ssupick_be.domain.payment.dto.response.PortOnePaymentResponse;
import com.ssupick.ssupick_be.domain.payment.entity.Payment;
import com.ssupick.ssupick_be.domain.payment.enums.CouponProduct;
import com.ssupick.ssupick_be.domain.payment.repository.PaymentRepository;
import com.ssupick.ssupick_be.domain.user.entity.User;
import com.ssupick.ssupick_be.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final String PAID_STATUS = "PAID";

    private final PortOneClient portOneClient;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    // 프론트 결제 화면에 표시할 쿠폰 상품 목록을 조회합니다.
    @Transactional(readOnly = true)
    public List<CouponProductResponse> getCouponProducts() {
        return Arrays.stream(CouponProduct.values())
                .map(CouponProductResponse::from)
                .toList();
    }

    // PortOne 결제를 검증하고 결제 상품에 해당하는 쿠폰을 충전합니다.
    @Transactional
    public PaymentVerifyResponse verifyPayment(Long userId, String paymentId, PaymentVerifyRequest request) {
        User user = userRepository.findByIdAndDeletedFalse(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND));
        CouponProduct couponProduct = request.couponProduct();

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

        PortOnePaymentResponse payment = portOneClient.getPayment(paymentId);

        if (!PAID_STATUS.equals(payment.status())) {
            throw new GeneralException(ErrorStatus.PAYMENT_STATUS_INVALID);
        }

        Long actualAmount = payment.amount() != null ? payment.amount().total() : null;
        if (!couponProduct.getPrice().equals(actualAmount)) {
            throw new GeneralException(ErrorStatus.PAYMENT_AMOUNT_MISMATCH);
        }

        user.increaseCouponCount(couponProduct.getCouponCount());
        paymentRepository.save(Payment.paid(paymentId, user, couponProduct, actualAmount));

        return PaymentVerifyResponse.from(
                payment,
                couponProduct.getCouponCount(),
                user.getRemainingCouponCount()
        );
    }
}

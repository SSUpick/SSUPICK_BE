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
import com.ssupick.ssupick_be.domain.payment.service.PaymentWriter;
import com.ssupick.ssupick_be.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final String PAID_STATUS = "PAID";

    private final PortOneClient portOneClient;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final PaymentWriter paymentWriter;

    // 프론트 결제 화면에 표시할 쿠폰 상품 목록을 조회합니다.
    @Transactional(readOnly = true)
    public List<CouponProductResponse> getCouponProducts() {
        return Arrays.stream(CouponProduct.values())
                .map(CouponProductResponse::from)
                .toList();
    }

    // 외부 API 호출과 검증은 트랜잭션 밖에서 수행하고, DB 반영은 PaymentWriter에 위임합니다.
    public PaymentVerifyResponse verifyPayment(Long userId, String paymentId, PaymentVerifyRequest request) {
        CouponProduct couponProduct = request.couponProduct();

        // 1. DB에서 기존 결제 확인 — 있으면 PortOne 호출 없이 바로 반환합니다.
        Optional<Payment> existingPayment = paymentRepository.findByPaymentIdAndUserId(paymentId, userId);
        if (existingPayment.isPresent()) {
            Payment payment = existingPayment.get();
            int remainingCouponCount = userRepository.findById(userId)
                    .orElseThrow(() -> new GeneralException(ErrorStatus.USER_NOT_FOUND))
                    .getRemainingCouponCount();
            return new PaymentVerifyResponse(
                    payment.getPaymentId(),
                    payment.getStatus().name(),
                    payment.getPaidAmount(),
                    0,
                    remainingCouponCount
            );
        }

        // 2. PortOne 외부 API 호출 + 순수 검증 (트랜잭션 밖)
        PortOnePaymentResponse portOnePayment = portOneClient.getPayment(paymentId);
        validatePayment(portOnePayment, couponProduct);

        // 3. DB 저장 + 쿠폰 충전 (트랜잭션 안)
        return paymentWriter.completePayment(userId, paymentId, couponProduct, portOnePayment);
    }

    // PortOne 응답의 결제 상태와 금액을 검증합니다.
    private void validatePayment(PortOnePaymentResponse payment, CouponProduct couponProduct) {
        if (!PAID_STATUS.equals(payment.status())) {
            throw new GeneralException(ErrorStatus.PAYMENT_STATUS_INVALID);
        }
        Long actualAmount = payment.amount() != null ? payment.amount().total() : null;
        if (!couponProduct.getPrice().equals(actualAmount)) {
            throw new GeneralException(ErrorStatus.PAYMENT_AMOUNT_MISMATCH);
        }
    }
}

package com.ssupick.ssupick_be.domain.payment.service;

import com.ssupick.ssupick_be.common.exception.GeneralException;
import com.ssupick.ssupick_be.common.status.ErrorStatus;
import com.ssupick.ssupick_be.domain.payment.client.PortOneClient;
import com.ssupick.ssupick_be.domain.payment.dto.request.PaymentVerifyRequest;
import com.ssupick.ssupick_be.domain.payment.dto.response.CouponProductResponse;
import com.ssupick.ssupick_be.domain.payment.dto.response.PaymentVerifyResponse;
import com.ssupick.ssupick_be.domain.payment.dto.response.PortOnePaymentResponse;
import com.ssupick.ssupick_be.domain.payment.enums.CouponProduct;
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

        // 트랜잭션 밖: PortOne 외부 API 호출 + 순수 검증
        PortOnePaymentResponse payment = portOneClient.getPayment(paymentId);
        validatePayment(payment, couponProduct);

        // 트랜잭션 안: DB 저장 + 쿠폰 충전
        return paymentWriter.completePayment(userId, paymentId, couponProduct, payment);
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

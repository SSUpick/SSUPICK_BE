package com.ssupick.ssupick_be.domain.payment.service;

import com.ssupick.ssupick_be.common.exception.GeneralException;
import com.ssupick.ssupick_be.common.status.ErrorStatus;
import com.ssupick.ssupick_be.domain.payment.client.PortOneClient;
import com.ssupick.ssupick_be.domain.payment.dto.request.PaymentVerifyRequest;
import com.ssupick.ssupick_be.domain.payment.dto.response.CouponProductResponse;
import com.ssupick.ssupick_be.domain.payment.dto.response.PaymentVerifyResponse;
import com.ssupick.ssupick_be.domain.payment.dto.response.PortOnePaymentResponse;
import com.ssupick.ssupick_be.domain.payment.enums.CouponProduct;
import com.ssupick.ssupick_be.domain.payment.repository.PaymentWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PortOneClient portOneClient;

    @Mock
    private PaymentWriter paymentWriter;

    @InjectMocks
    private PaymentService paymentService;

    // 쿠폰 상품 목록이 서버에 정의된 순서대로 반환되는지 검증합니다.
    @Test
    void getCouponProducts_returnsServerDefinedProducts() {
        List<CouponProductResponse> response = paymentService.getCouponProducts();

        assertThat(response).extracting(CouponProductResponse::productCode)
                .containsExactly("COUPON_1", "COUPON_4", "COUPON_8");
        assertThat(response).extracting(CouponProductResponse::price)
                .containsExactly(1000L, 3000L, 5000L);
    }

    // PortOne 결제 상태가 PAID이고 금액이 일치하면 PaymentWriter.completePayment를 호출합니다.
    @Test
    void verifyPayment_delegatesToPaymentWriterWhenValidPayment() {
        PortOnePaymentResponse portOneResponse = new PortOnePaymentResponse(
                "payment-1",
                "PAID",
                new PortOnePaymentResponse.Amount(3000L)
        );
        PaymentVerifyResponse expected = new PaymentVerifyResponse("payment-1", "PAID", 3000L, 4, 4);

        when(portOneClient.getPayment("payment-1")).thenReturn(portOneResponse);
        when(paymentWriter.completePayment(1L, "payment-1", CouponProduct.COUPON_4, portOneResponse))
                .thenReturn(expected);

        PaymentVerifyResponse response = paymentService.verifyPayment(
                1L, "payment-1", new PaymentVerifyRequest(CouponProduct.COUPON_4)
        );

        assertThat(response).isEqualTo(expected);
        verify(paymentWriter).completePayment(1L, "payment-1", CouponProduct.COUPON_4, portOneResponse);
    }

    // PortOne 결제 상태가 PAID가 아니면 예외를 던지고 PaymentWriter를 호출하지 않습니다.
    @Test
    void verifyPayment_throwsWhenPaymentStatusIsNotPaid() {
        when(portOneClient.getPayment("payment-1"))
                .thenReturn(new PortOnePaymentResponse(
                        "payment-1", "READY", new PortOnePaymentResponse.Amount(1000L)
                ));

        assertThatThrownBy(() -> paymentService.verifyPayment(
                1L, "payment-1", new PaymentVerifyRequest(CouponProduct.COUPON_1)
        ))
                .isInstanceOfSatisfying(GeneralException.class, e ->
                        assertThat(e.getErrorStatus()).isEqualTo(ErrorStatus.PAYMENT_STATUS_INVALID));

        verify(paymentWriter, never()).completePayment(any(), any(), any(), any());
    }

    // 금액이 상품 가격과 다르면 예외를 던지고 PaymentWriter를 호출하지 않습니다.
    @Test
    void verifyPayment_throwsWhenAmountDoesNotMatch() {
        when(portOneClient.getPayment("payment-1"))
                .thenReturn(new PortOnePaymentResponse(
                        "payment-1", "PAID", new PortOnePaymentResponse.Amount(9999L)
                ));

        assertThatThrownBy(() -> paymentService.verifyPayment(
                1L, "payment-1", new PaymentVerifyRequest(CouponProduct.COUPON_8)
        ))
                .isInstanceOfSatisfying(GeneralException.class, e ->
                        assertThat(e.getErrorStatus()).isEqualTo(ErrorStatus.PAYMENT_AMOUNT_MISMATCH));

        verify(paymentWriter, never()).completePayment(any(), any(), any(), any());
    }
}

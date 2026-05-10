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
import com.ssupick.ssupick_be.domain.payment.enums.PaymentStatus;
import com.ssupick.ssupick_be.domain.payment.properties.PortOneProperties;
import com.ssupick.ssupick_be.domain.payment.repository.PaymentRepository;
import com.ssupick.ssupick_be.domain.user.entity.User;
import com.ssupick.ssupick_be.domain.user.enums.DeviceType;
import com.ssupick.ssupick_be.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PortOneClient portOneClient;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentWriter paymentWriter;

    @Mock
    private PortOneProperties portOneProperties;

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

    // 결제창 HTML에 PortOne 리디렉션 완료 URL을 포함합니다.
    @Test
    void buildCheckoutHtml_includesRedirectUrl() {
        User user = User.createTestUser("test-user", DeviceType.IOS);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(portOneProperties.storeId()).thenReturn("test_store_id");
        when(portOneProperties.channelKey()).thenReturn("test_channel_key");
        when(portOneProperties.redirectUrl()).thenReturn("https://ssupick.love/payments/complete");

        String html = paymentService.buildCheckoutHtml(1L, CouponProduct.COUPON_1);

        assertThat(html).contains("redirectUrl: \"https://ssupick.love/payments/complete\"");
        verify(paymentRepository).save(any(Payment.class));
    }

    // 기존 결제가 PAID이면 PortOne 호출 없이 바로 기존 결과를 반환합니다.
    @Test
    void verifyPayment_returnsPaidPaymentWithoutCallingPortOne() {
        User user = User.createTestUser("test-user", DeviceType.IOS);
        Payment existingPayment = Payment.paid("payment-1", user, CouponProduct.COUPON_4, 3000L);

        when(paymentRepository.findByPaymentIdAndUserId("payment-1", 1L))
                .thenReturn(Optional.of(existingPayment));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        PaymentVerifyResponse response = paymentService.verifyPayment(
                1L, "payment-1", new PaymentVerifyRequest(CouponProduct.COUPON_4)
        );

        assertThat(response.paymentId()).isEqualTo("payment-1");
        assertThat(response.chargedCouponCount()).isEqualTo(0);
        verify(portOneClient, never()).getPayment(any());
        verify(paymentWriter, never()).completePayment(any(), any(), any(), any());
    }

    // 기존 결제가 READY이면 PortOne을 호출하고 PaymentWriter에 위임합니다.
    @Test
    void verifyPayment_delegatesToPaymentWriterWhenPaymentIsReady() {
        User user = User.createTestUser("test-user", DeviceType.IOS);
        Payment readyPayment = Payment.ready("payment-1", user, CouponProduct.COUPON_4);
        PortOnePaymentResponse portOneResponse = new PortOnePaymentResponse(
                "payment-1", "PAID", new PortOnePaymentResponse.Amount(3000L)
        );
        PaymentVerifyResponse expected = new PaymentVerifyResponse("payment-1", "PAID", 3000L, 4, 4);

        when(paymentRepository.findByPaymentIdAndUserId("payment-1", 1L))
                .thenReturn(Optional.of(readyPayment));
        when(portOneClient.getPayment("payment-1")).thenReturn(portOneResponse);
        when(paymentWriter.completePayment(1L, "payment-1", CouponProduct.COUPON_4, portOneResponse))
                .thenReturn(expected);

        PaymentVerifyResponse response = paymentService.verifyPayment(
                1L, "payment-1", new PaymentVerifyRequest(CouponProduct.COUPON_4)
        );

        assertThat(response).isEqualTo(expected);
        assertThat(readyPayment.getStatus()).isEqualTo(PaymentStatus.READY);
        verify(paymentWriter).completePayment(1L, "payment-1", CouponProduct.COUPON_4, portOneResponse);
    }

    // 기존 결제가 없으면 PortOne을 호출하고 PaymentWriter에 위임합니다.
    @Test
    void verifyPayment_delegatesToPaymentWriterWhenNewPayment() {
        PortOnePaymentResponse portOneResponse = new PortOnePaymentResponse(
                "payment-1", "PAID", new PortOnePaymentResponse.Amount(3000L)
        );
        PaymentVerifyResponse expected = new PaymentVerifyResponse("payment-1", "PAID", 3000L, 4, 4);

        when(paymentRepository.findByPaymentIdAndUserId("payment-1", 1L)).thenReturn(Optional.empty());
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
        when(paymentRepository.findByPaymentIdAndUserId("payment-1", 1L)).thenReturn(Optional.empty());
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
        when(paymentRepository.findByPaymentIdAndUserId("payment-1", 1L)).thenReturn(Optional.empty());
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

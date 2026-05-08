package com.ssupick.ssupick_be.domain.payment.service;

import com.ssupick.ssupick_be.common.exception.GeneralException;
import com.ssupick.ssupick_be.common.status.ErrorStatus;
import com.ssupick.ssupick_be.domain.payment.client.PortOneClient;
import com.ssupick.ssupick_be.domain.payment.dto.request.PaymentVerifyRequest;
import com.ssupick.ssupick_be.domain.payment.dto.response.CouponProductResponse;
import com.ssupick.ssupick_be.domain.payment.dto.response.PaymentVerifyResponse;
import com.ssupick.ssupick_be.domain.payment.dto.response.PortOnePaymentResponse;
import com.ssupick.ssupick_be.domain.payment.enums.CouponProduct;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PortOneClient portOneClient;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void getCouponProducts_returnsServerDefinedProducts() {
        List<CouponProductResponse> response = paymentService.getCouponProducts();

        assertThat(response).extracting(CouponProductResponse::productCode)
                .containsExactly("COUPON_1", "COUPON_4", "COUPON_8");
        assertThat(response).extracting(CouponProductResponse::price)
                .containsExactly(1000L, 3000L, 5000L);
    }

    @Test
    void verifyPayment_chargesCouponWhenStatusIsPaidAndAmountMatchesProduct() {
        User user = User.createTestUser("payment-test-user", DeviceType.IOS);
        when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(user));
        when(paymentRepository.findByPaymentId("payment-1")).thenReturn(Optional.empty());
        when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(portOneClient.getPayment("payment-1"))
                .thenReturn(new PortOnePaymentResponse(
                        "payment-1",
                        "PAID",
                        new PortOnePaymentResponse.Amount(3000L)
                ));

        PaymentVerifyResponse response = paymentService.verifyPayment(
                1L,
                "payment-1",
                new PaymentVerifyRequest(CouponProduct.COUPON_4)
        );

        assertThat(response.paymentId()).isEqualTo("payment-1");
        assertThat(response.status()).isEqualTo("PAID");
        assertThat(response.totalAmount()).isEqualTo(3000L);
        assertThat(response.chargedCouponCount()).isEqualTo(4);
        assertThat(response.remainingCouponCount()).isEqualTo(4);
        assertThat(user.getRemainingCouponCount()).isEqualTo(4);
    }

    @Test
    void verifyPayment_throwsWhenPaymentStatusIsNotPaid() {
        User user = User.createTestUser("payment-test-user", DeviceType.IOS);
        when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(user));
        when(paymentRepository.findByPaymentId("payment-1")).thenReturn(Optional.empty());
        when(portOneClient.getPayment("payment-1"))
                .thenReturn(new PortOnePaymentResponse(
                        "payment-1",
                        "READY",
                        new PortOnePaymentResponse.Amount(4900L)
                ));

        assertThatThrownBy(() -> paymentService.verifyPayment(
                1L,
                "payment-1",
                new PaymentVerifyRequest(CouponProduct.COUPON_1)
        ))
                .isInstanceOfSatisfying(GeneralException.class, e ->
                        assertThat(e.getErrorStatus()).isEqualTo(ErrorStatus.PAYMENT_STATUS_INVALID));
    }

    @Test
    void verifyPayment_throwsWhenAmountDoesNotMatch() {
        User user = User.createTestUser("payment-test-user", DeviceType.IOS);
        when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(user));
        when(paymentRepository.findByPaymentId("payment-1")).thenReturn(Optional.empty());
        when(portOneClient.getPayment("payment-1"))
                .thenReturn(new PortOnePaymentResponse(
                        "payment-1",
                        "PAID",
                        new PortOnePaymentResponse.Amount(4900L)
                ));

        assertThatThrownBy(() -> paymentService.verifyPayment(
                1L,
                "payment-1",
                new PaymentVerifyRequest(CouponProduct.COUPON_8)
        ))
                .isInstanceOfSatisfying(GeneralException.class, e ->
                        assertThat(e.getErrorStatus()).isEqualTo(ErrorStatus.PAYMENT_AMOUNT_MISMATCH));
    }
}

package com.ssupick.ssupick_be.domain.payment.repository;

import com.ssupick.ssupick_be.common.exception.GeneralException;
import com.ssupick.ssupick_be.common.status.ErrorStatus;
import com.ssupick.ssupick_be.domain.payment.dto.response.PaymentVerifyResponse;
import com.ssupick.ssupick_be.domain.payment.dto.response.PortOnePaymentResponse;
import com.ssupick.ssupick_be.domain.payment.entity.Payment;
import com.ssupick.ssupick_be.domain.payment.enums.CouponProduct;
import com.ssupick.ssupick_be.domain.payment.enums.PaymentStatus;
import com.ssupick.ssupick_be.domain.payment.service.PaymentWriter;
import com.ssupick.ssupick_be.domain.user.entity.User;
import com.ssupick.ssupick_be.domain.user.enums.DeviceType;
import com.ssupick.ssupick_be.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentWriterTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PaymentWriter paymentWriter;

    private static final PortOnePaymentResponse PAID_RESPONSE = new PortOnePaymentResponse(
            "payment-1", "PAID", new PortOnePaymentResponse.Amount(3000L)
    );

    // READY -> PAID 전환 성공 시 쿠폰이 충전되고 올바른 응답이 반환되는지 검증합니다.
    @Test
    void completePayment_chargesCouponWhenMarkPaidSucceeds() {
        User user = User.createTestUser("test-user", DeviceType.IOS);

        when(paymentRepository.markReadyPaymentAsPaid(
                "payment-1", 1L, CouponProduct.COUPON_4, 3000L, 4, PaymentStatus.READY, PaymentStatus.PAID
        )).thenReturn(1);
        when(userRepository.increaseCouponCount(1L, 4)).thenReturn(1);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        PaymentVerifyResponse response = paymentWriter.completePayment(
                1L, "payment-1", CouponProduct.COUPON_4, PAID_RESPONSE
        );

        assertThat(response.paymentId()).isEqualTo("payment-1");
        assertThat(response.status()).isEqualTo("PAID");
        assertThat(response.totalAmount()).isEqualTo(3000L);
        assertThat(response.chargedCouponCount()).isEqualTo(4);
        verify(userRepository).increaseCouponCount(1L, 4);
    }

    // PAID 전환 성공 후 쿠폰 증가 update가 0을 반환하면 USER_NOT_FOUND 예외를 던집니다.
    @Test
    void completePayment_throwsWhenCouponUpdateReturnsZero() {
        when(paymentRepository.markReadyPaymentAsPaid(
                "payment-1", 1L, CouponProduct.COUPON_4, 3000L, 4, PaymentStatus.READY, PaymentStatus.PAID
        )).thenReturn(1);
        when(userRepository.increaseCouponCount(1L, 4)).thenReturn(0);

        assertThatThrownBy(() -> paymentWriter.completePayment(
                1L, "payment-1", CouponProduct.COUPON_4, PAID_RESPONSE
        ))
                .isInstanceOfSatisfying(GeneralException.class, e ->
                        assertThat(e.getErrorStatus()).isEqualTo(ErrorStatus.USER_NOT_FOUND));
    }

    // PAID 전환 실패 시 이미 PAID인 결제라면 기존 결제 결과를 반환하는지 검증합니다.
    @Test
    void completePayment_returnsExistingPaymentWhenAlreadyPaid() {
        User user = User.createTestUser("test-user", DeviceType.IOS);
        Payment existingPayment = Payment.paid("payment-1", user, CouponProduct.COUPON_4, 3000L);

        when(paymentRepository.markReadyPaymentAsPaid(
                "payment-1", 1L, CouponProduct.COUPON_4, 3000L, 4, PaymentStatus.READY, PaymentStatus.PAID
        )).thenReturn(0);
        when(paymentRepository.findByPaymentIdAndUserId("payment-1", 1L))
                .thenReturn(Optional.of(existingPayment));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        PaymentVerifyResponse response = paymentWriter.completePayment(
                1L, "payment-1", CouponProduct.COUPON_4, PAID_RESPONSE
        );

        assertThat(response.paymentId()).isEqualTo("payment-1");
        assertThat(response.chargedCouponCount()).isEqualTo(0);
        assertThat(response.status()).isEqualTo(PaymentStatus.PAID.name());
        verify(userRepository, never()).increaseCouponCount(anyLong(), anyInt());
    }

    // PAID 전환 실패 시 다른 유저의 paymentId로 요청하면 PAYMENT_ALREADY_PROCESSED 예외를 던집니다.
    @Test
    void completePayment_throwsWhenPaymentBelongsToDifferentUser() {
        when(paymentRepository.markReadyPaymentAsPaid(
                "payment-1", 2L, CouponProduct.COUPON_4, 3000L, 4, PaymentStatus.READY, PaymentStatus.PAID
        )).thenReturn(0);
        when(paymentRepository.findByPaymentIdAndUserId("payment-1", 2L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentWriter.completePayment(
                2L, "payment-1", CouponProduct.COUPON_4, PAID_RESPONSE
        ))
                .isInstanceOfSatisfying(GeneralException.class, e ->
                        assertThat(e.getErrorStatus()).isEqualTo(ErrorStatus.PAYMENT_ALREADY_PROCESSED));

        verify(userRepository, never()).increaseCouponCount(anyLong(), anyInt());
    }

    // PAID 전환 실패 시 payment 기록 자체가 없으면 PAYMENT_ALREADY_PROCESSED 예외를 던집니다.
    @Test
    void completePayment_throwsWhenPaymentNotFound() {
        when(paymentRepository.markReadyPaymentAsPaid(
                "payment-1", 1L, CouponProduct.COUPON_4, 3000L, 4, PaymentStatus.READY, PaymentStatus.PAID
        )).thenReturn(0);
        when(paymentRepository.findByPaymentIdAndUserId("payment-1", 1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentWriter.completePayment(
                1L, "payment-1", CouponProduct.COUPON_4, PAID_RESPONSE
        ))
                .isInstanceOfSatisfying(GeneralException.class, e ->
                        assertThat(e.getErrorStatus()).isEqualTo(ErrorStatus.PAYMENT_ALREADY_PROCESSED));

        verify(userRepository, never()).increaseCouponCount(anyLong(), anyInt());
    }

    // PAID 전환 실패 시 기존 결제가 READY이면 아직 완료 처리되지 않은 상태이므로 상태 오류를 던집니다.
    @Test
    void completePayment_throwsWhenExistingPaymentIsStillReady() {
        User user = User.createTestUser("test-user", DeviceType.IOS);
        Payment readyPayment = Payment.ready("payment-1", user, CouponProduct.COUPON_4);

        when(paymentRepository.markReadyPaymentAsPaid(
                "payment-1", 1L, CouponProduct.COUPON_4, 3000L, 4, PaymentStatus.READY, PaymentStatus.PAID
        )).thenReturn(0);
        when(paymentRepository.findByPaymentIdAndUserId("payment-1", 1L))
                .thenReturn(Optional.of(readyPayment));

        assertThatThrownBy(() -> paymentWriter.completePayment(
                1L, "payment-1", CouponProduct.COUPON_4, PAID_RESPONSE
        ))
                .isInstanceOfSatisfying(GeneralException.class, e ->
                        assertThat(e.getErrorStatus()).isEqualTo(ErrorStatus.PAYMENT_STATUS_INVALID));

        verify(userRepository, never()).increaseCouponCount(anyLong(), anyInt());
    }

    // PortOne 응답의 amount가 null이면 totalAmount가 null로 반환됩니다.
    @Test
    void completePayment_handlesNullAmount() {
        User user = User.createTestUser("test-user", DeviceType.IOS);
        PortOnePaymentResponse nullAmountResponse = new PortOnePaymentResponse("payment-1", "PAID", null);

        when(paymentRepository.markReadyPaymentAsPaid(
                "payment-1", 1L, CouponProduct.COUPON_1, null, 1, PaymentStatus.READY, PaymentStatus.PAID
        )).thenReturn(1);
        when(userRepository.increaseCouponCount(1L, 1)).thenReturn(1);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        PaymentVerifyResponse response = paymentWriter.completePayment(
                1L, "payment-1", CouponProduct.COUPON_1, nullAmountResponse
        );

        assertThat(response.totalAmount()).isNull();
    }
}

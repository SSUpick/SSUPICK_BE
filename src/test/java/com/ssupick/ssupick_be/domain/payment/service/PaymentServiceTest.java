package com.ssupick.ssupick_be.domain.payment.service;

import com.ssupick.ssupick_be.common.exception.GeneralException;
import com.ssupick.ssupick_be.common.status.ErrorStatus;
import com.ssupick.ssupick_be.domain.payment.client.PortOneClient;
import com.ssupick.ssupick_be.domain.payment.dto.response.PaymentVerifyResponse;
import com.ssupick.ssupick_be.domain.payment.dto.response.PortOnePaymentResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PortOneClient portOneClient;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void verifyPayment_returnsPaymentWhenStatusIsPaidAndAmountMatches() {
        when(portOneClient.getPayment("payment-1"))
                .thenReturn(new PortOnePaymentResponse(
                        "payment-1",
                        "PAID",
                        new PortOnePaymentResponse.Amount(4900L)
                ));

        PaymentVerifyResponse response = paymentService.verifyPayment("payment-1", 4900L);

        assertThat(response.paymentId()).isEqualTo("payment-1");
        assertThat(response.status()).isEqualTo("PAID");
        assertThat(response.totalAmount()).isEqualTo(4900L);
    }

    @Test
    void verifyPayment_throwsWhenPaymentStatusIsNotPaid() {
        when(portOneClient.getPayment("payment-1"))
                .thenReturn(new PortOnePaymentResponse(
                        "payment-1",
                        "READY",
                        new PortOnePaymentResponse.Amount(4900L)
                ));

        assertThatThrownBy(() -> paymentService.verifyPayment("payment-1", 4900L))
                .isInstanceOfSatisfying(GeneralException.class, e ->
                        assertThat(e.getErrorStatus()).isEqualTo(ErrorStatus.PAYMENT_STATUS_INVALID));
    }

    @Test
    void verifyPayment_throwsWhenAmountDoesNotMatch() {
        when(portOneClient.getPayment("payment-1"))
                .thenReturn(new PortOnePaymentResponse(
                        "payment-1",
                        "PAID",
                        new PortOnePaymentResponse.Amount(4900L)
                ));

        assertThatThrownBy(() -> paymentService.verifyPayment("payment-1", 5900L))
                .isInstanceOfSatisfying(GeneralException.class, e ->
                        assertThat(e.getErrorStatus()).isEqualTo(ErrorStatus.PAYMENT_AMOUNT_MISMATCH));
    }
}

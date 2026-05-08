package com.ssupick.ssupick_be.domain.payment.service;

import com.ssupick.ssupick_be.common.exception.GeneralException;
import com.ssupick.ssupick_be.common.status.ErrorStatus;
import com.ssupick.ssupick_be.domain.payment.client.PortOneClient;
import com.ssupick.ssupick_be.domain.payment.dto.response.PaymentVerifyResponse;
import com.ssupick.ssupick_be.domain.payment.dto.response.PortOnePaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final String PAID_STATUS = "PAID";

    private final PortOneClient portOneClient;

    public PaymentVerifyResponse verifyPayment(String paymentId, Long expectedAmount) {
        PortOnePaymentResponse payment = portOneClient.getPayment(paymentId);

        if (!PAID_STATUS.equals(payment.status())) {
            throw new GeneralException(ErrorStatus.PAYMENT_STATUS_INVALID);
        }

        Long actualAmount = payment.amount() != null ? payment.amount().total() : null;
        if (expectedAmount != null && !expectedAmount.equals(actualAmount)) {
            throw new GeneralException(ErrorStatus.PAYMENT_AMOUNT_MISMATCH);
        }

        return PaymentVerifyResponse.from(payment);
    }
}

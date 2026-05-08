package com.ssupick.ssupick_be.domain.payment.controller;

import com.ssupick.ssupick_be.common.response.ApiResponse;
import com.ssupick.ssupick_be.common.status.SuccessStatus;
import com.ssupick.ssupick_be.domain.payment.controller.docs.PaymentControllerDocs;
import com.ssupick.ssupick_be.domain.payment.dto.response.PaymentVerifyResponse;
import com.ssupick.ssupick_be.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController implements PaymentControllerDocs {

    private final PaymentService paymentService;

    @PostMapping("/{paymentId}/verify")
    @Override
    public ResponseEntity<ApiResponse<PaymentVerifyResponse>> verifyPayment(
            @AuthenticationPrincipal Long userId,
            @PathVariable String paymentId,
            @RequestParam(required = false) Long expectedAmount
    ) {
        PaymentVerifyResponse response = paymentService.verifyPayment(paymentId, expectedAmount);
        return ApiResponse.success(SuccessStatus.PAYMENT_VERIFY_SUCCESS, response);
    }
}

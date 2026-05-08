package com.ssupick.ssupick_be.domain.payment.controller;

import com.ssupick.ssupick_be.common.response.ApiResponse;
import com.ssupick.ssupick_be.common.status.SuccessStatus;
import com.ssupick.ssupick_be.domain.payment.controller.docs.PaymentControllerDocs;
import com.ssupick.ssupick_be.domain.payment.dto.request.PaymentVerifyRequest;
import com.ssupick.ssupick_be.domain.payment.dto.response.CouponProductResponse;
import com.ssupick.ssupick_be.domain.payment.dto.response.PaymentVerifyResponse;
import com.ssupick.ssupick_be.domain.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController implements PaymentControllerDocs {

    private final PaymentService paymentService;

    // 쿠폰 결제 화면에서 사용할 상품 목록을 반환합니다.
    @GetMapping("/coupon-products")
    @Override
    public ResponseEntity<ApiResponse<List<CouponProductResponse>>> getCouponProducts() {
        List<CouponProductResponse> response = paymentService.getCouponProducts();
        return ApiResponse.success(SuccessStatus.COUPON_PRODUCT_LIST_SUCCESS, response);
    }

    // 결제 완료 후 PortOne 결제 정보를 검증하고 쿠폰을 충전합니다.
    @PostMapping("/{paymentId}/verify")
    @Override
    public ResponseEntity<ApiResponse<PaymentVerifyResponse>> verifyPayment(
            @AuthenticationPrincipal Long userId,
            @PathVariable String paymentId,
            @RequestBody @Valid PaymentVerifyRequest request
    ) {
        PaymentVerifyResponse response = paymentService.verifyPayment(userId, paymentId, request);
        return ApiResponse.success(SuccessStatus.PAYMENT_VERIFY_SUCCESS, response);
    }
}

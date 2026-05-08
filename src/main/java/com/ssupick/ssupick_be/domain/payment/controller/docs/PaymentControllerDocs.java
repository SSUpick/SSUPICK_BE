package com.ssupick.ssupick_be.domain.payment.controller.docs;

import com.ssupick.ssupick_be.common.response.ApiResponse;
import com.ssupick.ssupick_be.domain.payment.dto.request.PaymentVerifyRequest;
import com.ssupick.ssupick_be.domain.payment.dto.response.CouponProductResponse;
import com.ssupick.ssupick_be.domain.payment.dto.response.PaymentVerifyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Payment", description = "결제 API")
public interface PaymentControllerDocs {

    // 쿠폰 결제 화면에서 사용할 상품 목록 API 문서를 정의합니다.
    @Operation(summary = "쿠폰 상품 목록 조회", description = "프론트 결제 화면에 표시할 쿠폰 상품 코드, 가격, 지급 수량을 조회합니다.")
    ResponseEntity<ApiResponse<List<CouponProductResponse>>> getCouponProducts();

    // 결제 검증 및 쿠폰 충전 API 문서를 정의합니다.
    @Operation(summary = "결제 검증", description = "PortOne 결제 단건 조회 결과를 기반으로 결제 상태와 금액을 검증합니다.")
    ResponseEntity<ApiResponse<PaymentVerifyResponse>> verifyPayment(
            Long userId,
            String paymentId,
            PaymentVerifyRequest request
    );
}

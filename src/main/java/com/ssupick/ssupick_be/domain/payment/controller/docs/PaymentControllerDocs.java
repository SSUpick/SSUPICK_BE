package com.ssupick.ssupick_be.domain.payment.controller.docs;

import com.ssupick.ssupick_be.common.response.ApiResponse;
import com.ssupick.ssupick_be.domain.payment.dto.response.PaymentVerifyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Payment", description = "결제 API")
public interface PaymentControllerDocs {

    @Operation(summary = "결제 검증", description = "PortOne 결제 단건 조회 결과를 기반으로 결제 상태와 금액을 검증합니다.")
    ResponseEntity<ApiResponse<PaymentVerifyResponse>> verifyPayment(
            Long userId,
            String paymentId,
            Long expectedAmount
    );
}

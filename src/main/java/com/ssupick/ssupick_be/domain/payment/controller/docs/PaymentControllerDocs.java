package com.ssupick.ssupick_be.domain.payment.controller.docs;

import com.ssupick.ssupick_be.common.response.ApiResponse;
import com.ssupick.ssupick_be.domain.payment.dto.request.PaymentVerifyRequest;
import com.ssupick.ssupick_be.domain.payment.dto.response.CouponProductResponse;
import com.ssupick.ssupick_be.domain.payment.dto.response.PaymentVerifyResponse;
import com.ssupick.ssupick_be.domain.payment.enums.CouponProduct;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Payment", description = "결제 API")
public interface PaymentControllerDocs {

    @Operation(summary = "쿠폰 상품 목록 조회", description = "프론트 결제 화면에 표시할 쿠폰 상품 코드, 가격, 지급 수량을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상품 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = CouponProductResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    ResponseEntity<ApiResponse<List<CouponProductResponse>>> getCouponProducts();

    @Operation(summary = "결제 HTML 생성", description = "READY 결제 기록을 생성하고 PortOne Browser SDK로 결제창을 여는 테스트용 HTML을 반환합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "HTML 반환 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 쿠폰 상품",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    ResponseEntity<String> getCheckoutPage(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Parameter(description = "쿠폰 상품 코드 (COUPON_1 / COUPON_4 / COUPON_8)", required = true)
            @RequestParam CouponProduct couponProduct
    );

    @Operation(summary = "결제 검증 및 쿠폰 충전",
            description = """
                    PortOne 결제 단건 조회 결과를 기반으로 결제 상태와 금액을 검증하고 쿠폰을 충전합니다.
                    
                    - 이미 처리된 결제(paymentId 중복)이면 기존 결제 결과를 그대로 반환합니다.
                    - 결제 상태가 PAID가 아니거나 금액이 상품 가격과 다르면 실패합니다.
                    """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "결제 검증 및 쿠폰 충전 성공",
                    content = @Content(schema = @Schema(implementation = PaymentVerifyResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청, 결제 상태 불일치 또는 금액 불일치",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 처리된 결제",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    ResponseEntity<ApiResponse<PaymentVerifyResponse>> verifyPayment(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Parameter(description = "PortOne 결제 고유 ID", required = true) @PathVariable String paymentId,
            @RequestBody PaymentVerifyRequest request
    );
}

package com.ssupick.ssupick_be.domain.admin.controller.docs;

import com.ssupick.ssupick_be.common.response.ApiResponse;
import com.ssupick.ssupick_be.domain.admin.dto.request.AdminCouponAddRequest;
import com.ssupick.ssupick_be.domain.admin.dto.request.AdminGenerationCountSetRequest;
import com.ssupick.ssupick_be.domain.admin.dto.request.AdminLoginRequest;
import com.ssupick.ssupick_be.domain.admin.dto.response.AdminCouponAddResponse;
import com.ssupick.ssupick_be.domain.admin.dto.response.AdminGenerationCountSetResponse;
import com.ssupick.ssupick_be.domain.admin.dto.response.AdminLoginResponse;
import com.ssupick.ssupick_be.domain.admin.dto.response.AdminUserSearchResponse;
import com.ssupick.ssupick_be.domain.bank.dto.request.BankDepositAssignRequest;
import com.ssupick.ssupick_be.domain.bank.dto.response.BankDepositEventResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Admin", description = "관리자 API")
public interface AdminControllerDocs {

    @Operation(
            summary = "관리자 로그인",
            description = "관리자 전용 계정으로 로그인하고 관리자 API 접근용 access token을 발급합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "관리자 로그인 성공",
                    content = @Content(schema = @Schema(implementation = AdminLoginResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (username, password 누락)",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "관리자 인증 실패",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    ResponseEntity<ApiResponse<AdminLoginResponse>> login(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody AdminLoginRequest request
    );

    @Operation(
            summary = "관리자 유저 검색",
            description = "userId, 이름, 닉네임, 이메일, 전화번호로 유저를 검색합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "관리자 유저 검색 성공",
                    content = @Content(schema = @Schema(implementation = AdminUserSearchResponse.class))
            )
    })
    ResponseEntity<ApiResponse<List<AdminUserSearchResponse>>> searchUsers(
            @Parameter(description = "검색어")
            @RequestParam(required = false) String keyword
    );

    @Operation(
            summary = "관리자 쿠폰 수동 충전",
            description = "관리자가 선택한 유저 ID와 쿠폰 상품으로 쿠폰을 수동 충전하고 감사 로그를 남깁니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "관리자 쿠폰 충전 성공",
                    content = @Content(schema = @Schema(implementation = AdminCouponAddResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "유저 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    ResponseEntity<ApiResponse<AdminCouponAddResponse>> addCoupon(
            @Valid @RequestBody AdminCouponAddRequest request
    );

    @Operation(
            summary = "관리자 이미지 생성 횟수 수동 설정",
            description = "관리자가 유저 닉네임으로 유저를 찾아 이미지 생성 잔여 횟수를 지정한 값으로 설정합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "관리자 이미지 생성 횟수 설정 성공",
                    content = @Content(schema = @Schema(implementation = AdminGenerationCountSetResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (닉네임 누락, 생성 횟수 음수)",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "유저 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    ResponseEntity<ApiResponse<AdminGenerationCountSetResponse>> setGenerationCount(
            @Valid @RequestBody AdminGenerationCountSetRequest request
    );

    @Operation(
            summary = "관리자 입금 이벤트 목록 조회",
            description = "자동 처리되지 않았거나 검토가 필요한 입금 이벤트 목록을 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "입금 이벤트 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = BankDepositEventResponse.class))
            )
    })
    ResponseEntity<ApiResponse<List<BankDepositEventResponse>>> getDepositEvents();

    @Operation(
            summary = "관리자 입금 이벤트 수동 매칭",
            description = "관리자가 입금 이벤트를 유저와 수동 매칭하고 쿠폰을 충전합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "입금 이벤트 수동 매칭 성공",
                    content = @Content(schema = @Schema(implementation = BankDepositEventResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "입금 이벤트 또는 유저 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    ResponseEntity<ApiResponse<BankDepositEventResponse>> assignDepositEvent(
            @Parameter(description = "입금 이벤트 ID", required = true)
            @PathVariable Long eventId,
            @Valid @RequestBody BankDepositAssignRequest request
    );
}

package com.ssupick.ssupick_be.domain.admin.controller.docs;

import com.ssupick.ssupick_be.common.response.ApiResponse;
import com.ssupick.ssupick_be.domain.admin.dto.request.AdminCouponAddRequest;
import com.ssupick.ssupick_be.domain.admin.dto.request.AdminLoginRequest;
import com.ssupick.ssupick_be.domain.admin.dto.response.AdminCouponAddResponse;
import com.ssupick.ssupick_be.domain.admin.dto.response.AdminLoginResponse;
import com.ssupick.ssupick_be.domain.admin.dto.response.AdminUserSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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
}

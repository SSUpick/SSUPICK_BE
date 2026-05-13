package com.ssupick.ssupick_be.domain.user.controller.docs;

import com.ssupick.ssupick_be.common.response.ApiResponse;
import com.ssupick.ssupick_be.domain.user.dto.request.RegisterUserOnboardingRequest;
import com.ssupick.ssupick_be.domain.user.dto.request.UpdatePhoneNumberRequest;
import com.ssupick.ssupick_be.domain.user.dto.request.UpdateUserProfileRequest;
import com.ssupick.ssupick_be.domain.user.dto.request.ValidateNicknameRequest;
import com.ssupick.ssupick_be.domain.user.dto.response.GetTargetUserProfileResponse;
import com.ssupick.ssupick_be.domain.user.dto.response.GetUserCardResponse;
import com.ssupick.ssupick_be.domain.user.dto.response.GetUserProfileResponse;
import com.ssupick.ssupick_be.domain.user.dto.response.GetProfileViewListResponse;
import com.ssupick.ssupick_be.domain.user.dto.response.ValidateNicknameResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "User", description = "유저 관련 API")
public interface UserControllerDocs {

    @Operation(
            summary = "온보딩 프로필 등록",
            description = "닉네임, MBTI, 성별, 연락처, 어필 문구를 등록하고 온보딩을 완료합니다. 온보딩은 1회만 가능합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "프로필 등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (유효성 검사 실패 또는 어필 항목 오류)",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 온보딩 완료",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    ResponseEntity<ApiResponse<Void>> registerUserOnboarding(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @RequestBody @Valid RegisterUserOnboardingRequest request
    );

    @Operation(
            summary = "유저 카드 리스트 조회",
            description = """
                    온보딩을 완료한 유저 목록을 카드 형태로 조회합니다.
                    
                    - 인증된 요청: 본인을 제외한 목록을 반환합니다.
                    - 비인증 요청: 전체 목록을 반환합니다. (토큰 없이 호출 가능)
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "리스트 조회 성공",
                    content = @Content(schema = @Schema(implementation = GetUserCardResponse.class)))
    })
    ResponseEntity<ApiResponse<List<GetUserCardResponse>>> getUserCardList(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );

    @Operation(
            summary = "닉네임 검증",
            description = "닉네임의 길이, 비속어 포함 여부, 중복 여부를 검증합니다. 공백 포함 최대 7자까지 사용할 수 있습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "사용 가능한 닉네임",
                    content = @Content(schema = @Schema(implementation = ValidateNicknameResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 또는 비속어 포함",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 사용 중인 닉네임",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    ResponseEntity<ApiResponse<ValidateNicknameResponse>> validateNickname(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @RequestBody @Valid ValidateNicknameRequest request
    );

    @Operation(
            summary = "내 프로필 조회",
            description = "현재 로그인한 유저의 프로필 정보를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "프로필 조회 성공",
                    content = @Content(schema = @Schema(implementation = GetUserProfileResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    ResponseEntity<ApiResponse<GetUserProfileResponse>> getUserProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );

    @Operation(
            summary = "상대 유저 프로필 조회",
            description = """
                    특정 유저의 프로필을 조회합니다.
                    
                    - 첫 열람 시 쿠폰 1개를 차감합니다.
                    - 재열람 시 쿠폰을 차감하지 않고 열람 시간만 갱신합니다.
                    - 탈퇴 유저 및 온보딩 미완료 유저는 조회 불가합니다.
                    - 본인 프로필은 조회 불가합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "상대 프로필 조회 성공",
                    content = @Content(schema = @Schema(implementation = GetTargetUserProfileResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "본인 프로필 조회 불가",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "402", description = "쿠폰 부족",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "온보딩 미완료 유저",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    ResponseEntity<ApiResponse<GetTargetUserProfileResponse>> getTargetUserProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Parameter(description = "조회할 상대 유저 ID", required = true) @PathVariable Long targetUserId
    );

    @Operation(
            summary = "마이페이지 프로필 수정",
            description = "닉네임, MBTI, 어필 항목, 연락처를 수정합니다. 온보딩 완료 유저만 수정 가능합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "프로필 수정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (유효성 검사 실패)",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "온보딩 미완료 유저",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    ResponseEntity<ApiResponse<Void>> updateUserProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @RequestBody @Valid UpdateUserProfileRequest request
    );

    @Operation(
            summary = "결제용 전화번호 등록/수정",
            description = "쿠폰 결제에 사용할 전화번호를 하이픈 없이 등록하거나 수정합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "전화번호 등록 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (유효성 검사 실패)",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    ResponseEntity<ApiResponse<Void>> updatePhoneNumber(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @RequestBody @Valid UpdatePhoneNumberRequest request
    );

    @Operation(
            summary = "열람 목록 통합 조회",
            description = "내가 열람한 유저 목록과 나를 열람한 유저 목록을 한 번에 조회합니다. 각각 최신순으로 반환됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = GetProfileViewListResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    ResponseEntity<ApiResponse<GetProfileViewListResponse>> getProfileViewList(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId
    );
}

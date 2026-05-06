package com.ssupick.ssupick_be.domain.aiimage.controller.docs;

import com.ssupick.ssupick_be.common.response.ApiResponse;
import com.ssupick.ssupick_be.domain.aiimage.dto.response.AiImageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "AI Image", description = "AI 프로필 이미지 생성 관련 API")
public interface AiImageControllerDocs {

    @Operation(
            summary = "AI 이미지 생성",
            description = """
                    사용자의 원본 사진을 업로드하면 동물의 숲 스타일 3D 캐릭터 이미지를 생성합니다.
                    - 1인당 최대 3회 생성 가능 (서버에서 관리)
                    - 프론트에서 1024px로 리사이징 후 전송 권장
                    - 지원 형식: JPEG, PNG (최대 10MB)
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "이미지 생성 성공",
                    content = @Content(schema = @Schema(implementation = AiImageResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 파일 형식 또는 크기 초과",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "429", description = "이미지 생성 횟수 초과",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "502", description = "Gemini API 호출 실패",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    ResponseEntity<ApiResponse<AiImageResponse>> generateImage(
            @AuthenticationPrincipal Long userId,
            MultipartFile originalImage
    );

    @Operation(
            summary = "AI 이미지 목록 조회",
            description = "현재 유저가 생성한 AI 이미지 전체 목록을 최신순으로 반환합니다. 각 이미지는 7일 만료 Presigned URL로 제공됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = AiImageResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    ResponseEntity<ApiResponse<List<AiImageResponse>>> getImageList(
            @AuthenticationPrincipal Long userId
    );

    @Operation(
            summary = "최종 프로필 이미지 확정",
            description = """
                    생성된 이미지 중 하나를 최종 프로필 이미지로 선택합니다.
                    - 기존에 선택된 이미지가 있으면 자동으로 해제됩니다.
                    - 선택된 이미지는 프로필 카드의 대표 이미지로 반영됩니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "프로필 이미지 확정 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인 이미지가 아님",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "이미지를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    ResponseEntity<ApiResponse<Void>> selectProfileImage(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long aiImageId
    );
}

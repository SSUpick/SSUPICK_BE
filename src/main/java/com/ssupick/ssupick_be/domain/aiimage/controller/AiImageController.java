package com.ssupick.ssupick_be.domain.aiimage.controller;

import com.ssupick.ssupick_be.common.response.ApiResponse;
import com.ssupick.ssupick_be.common.status.SuccessStatus;
import com.ssupick.ssupick_be.domain.aiimage.controller.docs.AiImageControllerDocs;
import com.ssupick.ssupick_be.domain.aiimage.dto.response.AiImageResponse;
import com.ssupick.ssupick_be.domain.aiimage.service.AiImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/ai-images")
@RequiredArgsConstructor
public class AiImageController implements AiImageControllerDocs {

    private final AiImageService aiImageService;

    // 이미지 생성 — multipart/form-data 로 원본 사진 수신
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Override
    public ResponseEntity<ApiResponse<AiImageResponse>> generateImage(
            @AuthenticationPrincipal Long userId,
            @RequestPart("originalImage") MultipartFile originalImage
    ) {
        AiImageResponse response = aiImageService.generateImage(userId, originalImage);
        return ApiResponse.success(SuccessStatus.AI_IMAGE_GENERATE_SUCCESS, response);
    }

    // 생성 이미지 목록 조회
    @GetMapping
    @Override
    public ResponseEntity<ApiResponse<List<AiImageResponse>>> getImageList(
            @AuthenticationPrincipal Long userId
    ) {
        List<AiImageResponse> response = aiImageService.getImageList(userId);
        return ApiResponse.success(SuccessStatus.AI_IMAGE_LIST_SUCCESS, response);
    }

    // 단건 상태 조회 — 프론트가 PENDING → DONE 폴링 시 사용
    @GetMapping("/{aiImageId}/status")
    public ResponseEntity<ApiResponse<AiImageResponse>> getImageStatus(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long aiImageId
    ) {
        AiImageResponse response = aiImageService.getImageStatus(userId, aiImageId);
        return ApiResponse.success(SuccessStatus.AI_IMAGE_LIST_SUCCESS, response);
    }

    // 최종 프로필 이미지 확정
    @PatchMapping("/{aiImageId}/select")
    @Override
    public ResponseEntity<ApiResponse<Void>> selectProfileImage(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long aiImageId
    ) {
        aiImageService.selectProfileImage(userId, aiImageId);
        return ApiResponse.success(SuccessStatus.AI_IMAGE_SELECT_SUCCESS);
    }
}

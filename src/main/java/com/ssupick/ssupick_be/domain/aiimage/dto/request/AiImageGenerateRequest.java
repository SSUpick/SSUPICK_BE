package com.ssupick.ssupick_be.domain.aiimage.dto.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record AiImageGenerateRequest(

        @NotNull(message = "원본 이미지를 업로드해주세요.")
        MultipartFile originalImage
) {}

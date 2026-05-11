package com.ssupick.ssupick_be.domain.admin.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AdminLoginRequest(

        @NotBlank(message = "관리자 아이디는 필수입니다.")
        String username,

        @NotBlank(message = "관리자 비밀번호는 필수입니다.")
        String password
) {}

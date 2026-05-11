package com.ssupick.ssupick_be.domain.bank.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record BankDepositWebhookRequest(

        @NotBlank(message = "이벤트 키는 필수입니다.")
        String eventKey,

        @NotBlank(message = "입금자명은 필수입니다.")
        String depositorName,

        @NotNull(message = "입금액은 필수입니다.")
        @Positive(message = "입금액은 0보다 커야 합니다.")
        Long amount,

        LocalDateTime depositedAt,

        String rawText
) {}

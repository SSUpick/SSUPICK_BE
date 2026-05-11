package com.ssupick.ssupick_be.domain.bank.dto.response;

public record BankDepositWebhookResponse(
        Long eventId,
        String status,
        boolean duplicated
) {}

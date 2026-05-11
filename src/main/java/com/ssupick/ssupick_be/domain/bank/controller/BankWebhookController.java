package com.ssupick.ssupick_be.domain.bank.controller;

import com.ssupick.ssupick_be.common.response.ApiResponse;
import com.ssupick.ssupick_be.common.status.SuccessStatus;
import com.ssupick.ssupick_be.domain.bank.dto.request.BankDepositWebhookRequest;
import com.ssupick.ssupick_be.domain.bank.dto.response.BankDepositWebhookResponse;
import com.ssupick.ssupick_be.domain.bank.service.BankDepositService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhook/bank")
@RequiredArgsConstructor
public class BankWebhookController {

    private final BankDepositService bankDepositService;

    @PostMapping
    public ResponseEntity<ApiResponse<BankDepositWebhookResponse>> receive(
            @RequestHeader("Bank-Webhook-Secret") String webhookSecret,
            @Valid @RequestBody BankDepositWebhookRequest request
    ) {
        BankDepositWebhookResponse response = bankDepositService.receive(webhookSecret, request);
        return ApiResponse.success(SuccessStatus.BANK_DEPOSIT_WEBHOOK_SUCCESS, response);
    }
}

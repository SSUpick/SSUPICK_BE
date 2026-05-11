package com.ssupick.ssupick_be.domain.bank.dto.response;

import com.ssupick.ssupick_be.domain.bank.entity.BankDepositEvent;

import java.time.LocalDateTime;

public record BankDepositEventResponse(
        Long eventId,
        String eventKey,
        String bankName,
        String depositorName,
        Long amount,
        LocalDateTime depositedAt,
        String couponProduct,
        Long matchedUserId,
        String matchedUserName,
        String matchedUserNickname,
        String status,
        LocalDateTime processedAt
) {
    public static BankDepositEventResponse from(BankDepositEvent event) {
        return new BankDepositEventResponse(
                event.getId(),
                event.getEventKey(),
                event.getBankName(),
                event.getDepositorName(),
                event.getAmount(),
                event.getDepositedAt(),
                event.getCouponProduct() != null ? event.getCouponProduct().name() : null,
                event.getMatchedUser() != null ? event.getMatchedUser().getId() : null,
                event.getMatchedUser() != null ? event.getMatchedUser().getName() : null,
                event.getMatchedUser() != null ? event.getMatchedUser().getNickname() : null,
                event.getStatus().name(),
                event.getProcessedAt()
        );
    }
}

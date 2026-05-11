package com.ssupick.ssupick_be.domain.bank.entity;

import com.ssupick.ssupick_be.common.base.BaseEntity;
import com.ssupick.ssupick_be.domain.bank.enums.BankDepositEventStatus;
import com.ssupick.ssupick_be.domain.payment.enums.CouponProduct;
import com.ssupick.ssupick_be.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Table(
        name = "bank_deposit_event",
        uniqueConstraints = @UniqueConstraint(name = "uk_bank_deposit_event_key", columnNames = "event_key")
)
@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BankDepositEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_key", length = 100, nullable = false, unique = true)
    private String eventKey;

    @Column(name = "bank_name", length = 30, nullable = false)
    private String bankName;

    @Column(name = "depositor_name", length = 30, nullable = false)
    private String depositorName;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "deposited_at")
    private LocalDateTime depositedAt;

    @Column(name = "raw_text", length = 1000)
    private String rawText;

    @Enumerated(EnumType.STRING)
    @Column(name = "coupon_product", length = 30)
    private CouponProduct couponProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matched_user_id")
    private User matchedUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    private BankDepositEventStatus status;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    public static BankDepositEvent pending(
            String eventKey,
            String bankName,
            String depositorName,
            Long amount,
            LocalDateTime depositedAt,
            String rawText,
            CouponProduct couponProduct
    ) {
        return BankDepositEvent.builder()
                .eventKey(eventKey)
                .bankName(bankName)
                .depositorName(depositorName)
                .amount(amount)
                .depositedAt(depositedAt)
                .rawText(rawText)
                .couponProduct(couponProduct)
                .status(BankDepositEventStatus.PENDING)
                .build();
    }

    public void markAdminRequired() {
        this.status = BankDepositEventStatus.ADMIN_REQUIRED;
    }

    public void markAutoMatched(User user) {
        this.matchedUser = user;
        this.status = BankDepositEventStatus.AUTO_MATCHED;
    }

    public void markProcessed(User user, CouponProduct couponProduct) {
        this.matchedUser = user;
        this.couponProduct = couponProduct;
        this.status = BankDepositEventStatus.PROCESSED;
        this.processedAt = LocalDateTime.now();
    }
}

package com.ssupick.ssupick_be.domain.admin.entity;

import com.ssupick.ssupick_be.common.base.BaseEntity;
import com.ssupick.ssupick_be.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "admin_coupon_adjustment")
@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminCouponAdjustment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "delta_count", nullable = false)
    private int deltaCount;

    @Column(name = "remaining_coupon_count", nullable = false)
    private int remainingCouponCount;

    @Column(name = "reason", length = 50, nullable = false)
    private String reason;

    @Column(name = "depositor_name", length = 30)
    private String depositorName;

    @Column(name = "amount")
    private Long amount;

    @Column(name = "memo", length = 255)
    private String memo;

    public static AdminCouponAdjustment create(
            User user,
            int deltaCount,
            int remainingCouponCount,
            String reason,
            String depositorName,
            Long amount,
            String memo
    ) {
        return AdminCouponAdjustment.builder()
                .user(user)
                .deltaCount(deltaCount)
                .remainingCouponCount(remainingCouponCount)
                .reason(reason)
                .depositorName(depositorName)
                .amount(amount)
                .memo(memo)
                .build();
    }
}

package com.ssupick.ssupick_be.domain.payment.entity;

import com.ssupick.ssupick_be.common.base.BaseEntity;
import com.ssupick.ssupick_be.domain.payment.enums.CouponProduct;
import com.ssupick.ssupick_be.domain.payment.enums.PaymentStatus;
import com.ssupick.ssupick_be.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@Table(
        name = "payment",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_payment_id",
                columnNames = "payment_id"
        )
)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", nullable = false, length = 100)
    private String paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "coupon_product", nullable = false, length = 20)
    private CouponProduct couponProduct;

    @Column(name = "paid_amount", nullable = false)
    private Long paidAmount;

    @Column(name = "charged_coupon_count", nullable = false)
    private int chargedCouponCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    // 검증 완료된 결제 기록 엔티티를 생성합니다.
    public static Payment paid(
            String paymentId,
            User user,
            CouponProduct couponProduct,
            Long paidAmount
    ) {
        return Payment.builder()
                .paymentId(paymentId)
                .user(user)
                .couponProduct(couponProduct)
                .paidAmount(paidAmount)
                .chargedCouponCount(couponProduct.getCouponCount())
                .status(PaymentStatus.PAID)
                .build();
    }
}

package com.ssupick.ssupick_be.domain.payment.repository;

import com.ssupick.ssupick_be.domain.payment.entity.Payment;
import com.ssupick.ssupick_be.domain.payment.enums.CouponProduct;
import com.ssupick.ssupick_be.domain.payment.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // PortOne paymentId로 저장된 결제 기록을 조회합니다.
    Optional<Payment> findByPaymentId(String paymentId);

    // paymentId와 userId로 결제 기록을 조회합니다. 소유자 검증과 조회를 한 번에 처리합니다.
    @Query("SELECT p FROM Payment p WHERE p.paymentId = :paymentId AND p.user.id = :userId")
    Optional<Payment> findByPaymentIdAndUserId(
            @Param("paymentId") String paymentId,
            @Param("userId") Long userId
    );

    // READY 상태 결제 기록을 PAID로 원자 전환합니다. 반환값이 1이면 전환 성공, 0이면 이미 처리됐거나 대상 없음입니다.
    @Modifying
    @Query("""
            UPDATE Payment p
            SET p.status = :paidStatus,
                p.paidAmount = :paidAmount,
                p.chargedCouponCount = :chargedCouponCount
            WHERE p.paymentId = :paymentId
              AND p.user.id = :userId
              AND p.couponProduct = :couponProduct
              AND p.status = :readyStatus
            """)
    int markReadyPaymentAsPaid(
            @Param("paymentId") String paymentId,
            @Param("userId") Long userId,
            @Param("couponProduct") CouponProduct couponProduct,
            @Param("paidAmount") Long paidAmount,
            @Param("chargedCouponCount") int chargedCouponCount,
            @Param("readyStatus") PaymentStatus readyStatus,
            @Param("paidStatus") PaymentStatus paidStatus
    );

    // paymentId 중복 시 무시하고 삽입합니다. 반환값이 1이면 신규 삽입, 0이면 중복입니다.
    @Modifying
    @Query(value = """
            INSERT IGNORE INTO payment (
                payment_id,
                user_id,
                coupon_product,
                paid_amount,
                charged_coupon_count,
                status,
                created_at,
                updated_at
            ) VALUES (
                :paymentId,
                :userId,
                :couponProduct,
                :paidAmount,
                :chargedCouponCount,
                'PAID',
                NOW(),
                NOW()
            )
            """, nativeQuery = true)
    int insertIgnorePaidPayment(
            @Param("paymentId") String paymentId,
            @Param("userId") Long userId,
            @Param("couponProduct") String couponProduct,
            @Param("paidAmount") Long paidAmount,
            @Param("chargedCouponCount") int chargedCouponCount
    );
}

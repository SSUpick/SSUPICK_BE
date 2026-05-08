package com.ssupick.ssupick_be.domain.payment.repository;

import com.ssupick.ssupick_be.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // PortOne paymentId로 저장된 결제 기록을 조회합니다.
    Optional<Payment> findByPaymentId(String paymentId);
}

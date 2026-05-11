package com.ssupick.ssupick_be.domain.bank.repository;

import com.ssupick.ssupick_be.domain.bank.entity.BankDepositEvent;
import com.ssupick.ssupick_be.domain.bank.enums.BankDepositEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BankDepositEventRepository extends JpaRepository<BankDepositEvent, Long> {

    Optional<BankDepositEvent> findByEventKey(String eventKey);

    List<BankDepositEvent> findAllByStatusInOrderByCreatedAtDesc(Collection<BankDepositEventStatus> statuses);
}

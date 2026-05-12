package com.ssupick.ssupick_be.domain.bank.repository;

import com.ssupick.ssupick_be.domain.bank.entity.BankDepositEvent;
import com.ssupick.ssupick_be.domain.bank.enums.BankDepositEventStatus;
import com.ssupick.ssupick_be.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BankDepositEventRepository extends JpaRepository<BankDepositEvent, Long> {

    Optional<BankDepositEvent> findByEventKey(String eventKey);

    List<BankDepositEvent> findAllByStatusInOrderByCreatedAtDesc(Collection<BankDepositEventStatus> statuses);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE BankDepositEvent e SET e.matchedUser = null WHERE e.matchedUser = :user")
    void clearMatchedUser(User user);
}

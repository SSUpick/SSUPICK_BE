package com.ssupick.ssupick_be.domain.admin.repository;

import com.ssupick.ssupick_be.domain.admin.entity.AdminCouponAdjustment;
import com.ssupick.ssupick_be.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminCouponAdjustmentRepository extends JpaRepository<AdminCouponAdjustment, Long> {

    void deleteAllByUser(User user);
}

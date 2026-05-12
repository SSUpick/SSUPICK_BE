package com.ssupick.ssupick_be.domain.user.repository;

import com.ssupick.ssupick_be.domain.user.entity.User;
import com.ssupick.ssupick_be.domain.user.enums.OAuthProvider;
import com.ssupick.ssupick_be.domain.user.enums.OnboardingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByOauthIdAndOauthProvider(String oauthId, OAuthProvider oauthProvider);

    // 온보딩 완료 유저 전체 조회
    List<User> findAllByOnboardingStatusOrderByUpdatedAtDesc(OnboardingStatus onboardingStatus);

    // 온보딩 완료 유저 중 본인 제외 전체 조회
    List<User> findAllByOnboardingStatusAndIdNotOrderByUpdatedAtDesc(OnboardingStatus onboardingStatus, Long excludeId);

    boolean existsByNickname(String nickname);

    boolean existsByNicknameAndIdNot(String nickname, Long excludeId);

    @Query("""
            SELECT u
            FROM User u
            WHERE (:userId IS NOT NULL AND u.id = :userId)
               OR LOWER(COALESCE(u.nickname, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(COALESCE(u.name, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(COALESCE(u.email, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR COALESCE(u.phoneNumber, '') LIKE CONCAT('%', :keyword, '%')
            ORDER BY u.id DESC
            """)
    List<User> searchForAdmin(@Param("keyword") String keyword, @Param("userId") Long userId);

    @Query("""
            SELECT u
            FROM User u
            WHERE REPLACE(LOWER(COALESCE(u.nickname, '')), ' ', '') = :normalizedNickname
            """)
    List<User> findDepositNicknameMatches(@Param("normalizedNickname") String normalizedNickname);

    // 쿠폰 충전 — payment insert 성공 시에만 호출됩니다.
    @Modifying
    @Query("UPDATE User u SET u.remainingCouponCount = u.remainingCouponCount + :count WHERE u.id = :userId")
    int increaseCouponCount(@Param("userId") Long userId, @Param("count") int count);

    // 쿠폰 차감 — 쿠폰이 1개 이상 있을 때만 원자적으로 차감합니다.
    @Modifying
    @Query("""
            UPDATE User u
            SET u.remainingCouponCount = u.remainingCouponCount - 1
            WHERE u.id = :userId
              AND u.remainingCouponCount > 0
            """)
    int decreaseCouponCount(@Param("userId") Long userId);
}

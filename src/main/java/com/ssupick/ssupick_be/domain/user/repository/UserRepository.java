package com.ssupick.ssupick_be.domain.user.repository;

import com.ssupick.ssupick_be.domain.user.entity.User;
import com.ssupick.ssupick_be.domain.user.enums.OAuthProvider;
import com.ssupick.ssupick_be.domain.user.enums.OnboardingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByOauthIdAndOauthProvider(String oauthId, OAuthProvider oauthProvider);

    // 활성 유저 단건 조회 — 탈퇴 유저 제외
    Optional<User> findByIdAndDeletedFalse(Long id);

    // 온보딩 완료 + 탈퇴하지 않은 유저 중 본인 제외 전체 조회
    // findByOauthIdAndOauthProvider는 탈퇴 유저 복구 로직에서도 사용하므로 의도적으로 deletedFalse 미적용
    List<User> findAllByOnboardingStatusAndDeletedFalseAndIdNot(OnboardingStatus onboardingStatus, Long excludeId);
}

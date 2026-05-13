package com.ssupick.ssupick_be.domain.user.repository;

import com.ssupick.ssupick_be.domain.user.entity.WithdrawUser;
import com.ssupick.ssupick_be.domain.user.enums.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawUserRepository extends JpaRepository<WithdrawUser, Long> {

    boolean existsByOauthIdAndOauthProvider(String oauthId, OAuthProvider oauthProvider);
}

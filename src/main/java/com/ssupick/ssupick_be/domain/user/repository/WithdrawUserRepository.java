package com.ssupick.ssupick_be.domain.user.repository;

import com.ssupick.ssupick_be.domain.user.entity.WithdrawUser;
import com.ssupick.ssupick_be.domain.user.enums.OAuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WithdrawUserRepository extends JpaRepository<WithdrawUser, Long> {

    boolean existsByOauthIdAndOauthProvider(String oauthId, OAuthProvider oauthProvider);

    @Modifying
    @Query(value = """
            INSERT INTO withdraw_user (
                oauth_id,
                oauth_provider,
                withdrawn_at,
                created_at,
                updated_at
            ) VALUES (
                :oauthId,
                :oauthProvider,
                NOW(),
                NOW(),
                NOW()
            )
            ON DUPLICATE KEY UPDATE
                withdrawn_at = VALUES(withdrawn_at),
                updated_at = VALUES(updated_at)
            """, nativeQuery = true)
    int upsertWithdrawUser(
            @Param("oauthId") String oauthId,
            @Param("oauthProvider") String oauthProvider
    );
}

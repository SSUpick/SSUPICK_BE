package com.ssupick.ssupick_be.domain.user.entity;

import com.ssupick.ssupick_be.common.base.BaseEntity;
import com.ssupick.ssupick_be.domain.user.enums.OAuthProvider;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Table(
        name = "withdraw_user",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_withdraw_user_oauth",
                        columnNames = {"oauth_id", "oauth_provider"}
                )
        }
)
@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WithdrawUser extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "oauth_id", length = 255, nullable = false)
    private String oauthId;

    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider", length = 20, nullable = false)
    private OAuthProvider oauthProvider;

    @Column(name = "withdrawn_at", nullable = false)
    private LocalDateTime withdrawnAt;

    public static WithdrawUser from(User user) {
        return WithdrawUser.builder()
                .oauthId(user.getOauthId())
                .oauthProvider(user.getOauthProvider())
                .withdrawnAt(LocalDateTime.now())
                .build();
    }
}

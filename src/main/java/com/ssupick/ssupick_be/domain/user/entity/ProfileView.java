package com.ssupick.ssupick_be.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "profile_view",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_viewer_target",
                columnNames = {"viewer_id", "target_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfileView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viewer_id", nullable = false)
    private User viewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id", nullable = false)
    private User target;

    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;

    public ProfileView(User viewer, User target) {
        this.viewer = viewer;
        this.target = target;
        this.viewedAt = LocalDateTime.now();
    }

    // 재열람 시 시간 갱신 (upsert)
    public void updateViewedAt() {
        this.viewedAt = LocalDateTime.now();
    }
}

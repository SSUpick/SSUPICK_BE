package com.ssupick.ssupick_be.domain.user.repository;

import com.ssupick.ssupick_be.domain.user.entity.ProfileView;
import com.ssupick.ssupick_be.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProfileViewRepository extends JpaRepository<ProfileView, Long> {

    // upsert 여부 확인용
    Optional<ProfileView> findByViewerAndTarget(User viewer, User target);

    // 내가 열람한 사람 목록 — 최신순
    List<ProfileView> findByViewerOrderByViewedAtDesc(User viewer);

    // 나를 열람한 사람 목록 — 최신순
    List<ProfileView> findByTargetOrderByViewedAtDesc(User target);

    // 첫 열람 기록을 삽입합니다. 이미 같은 viewer-target 기록이 있으면 무시합니다.
    @Modifying
    @Query(value = """
            INSERT IGNORE INTO profile_view (
                viewer_id,
                target_id,
                viewed_at
            ) VALUES (
                :viewerId,
                :targetId,
                NOW()
            )
            """, nativeQuery = true)
    int insertIgnoreProfileView(
            @Param("viewerId") Long viewerId,
            @Param("targetId") Long targetId
    );

    // 재열람 시 열람 시간을 갱신합니다.
    @Modifying
    @Query("""
            UPDATE ProfileView pv
            SET pv.viewedAt = CURRENT_TIMESTAMP
            WHERE pv.viewer.id = :viewerId
              AND pv.target.id = :targetId
            """)
    int updateViewedAt(
            @Param("viewerId") Long viewerId,
            @Param("targetId") Long targetId
    );

    void deleteByViewerOrTarget(User viewer, User target);
}

package com.ssupick.ssupick_be.domain.user.repository;

import com.ssupick.ssupick_be.domain.user.entity.ProfileView;
import com.ssupick.ssupick_be.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProfileViewRepository extends JpaRepository<ProfileView, Long> {

    // upsert 여부 확인용
    Optional<ProfileView> findByViewerAndTarget(User viewer, User target);

    // 내가 열람한 사람 목록 — 최신순
    List<ProfileView> findByViewerOrderByViewedAtDesc(User viewer);

    // 나를 열람한 사람 목록 — 최신순
    List<ProfileView> findByTargetOrderByViewedAtDesc(User target);

    void deleteByViewerAndTarget(User viewer, User target);
}

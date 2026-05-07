package com.ssupick.ssupick_be.domain.aiimage.repository;

import com.ssupick.ssupick_be.domain.aiimage.entity.AiImage;
import com.ssupick.ssupick_be.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AiImageRepository extends JpaRepository<AiImage, Long> {

    // 유저의 전체 생성 이미지 목록 조회 (최신순)
    List<AiImage> findAllByUserOrderByCreatedAtDesc(User user);

    // 유저의 현재 선택된 이미지 조회
    Optional<AiImage> findByUserAndSelectedTrue(User user);

    // 유저의 선택된 이미지 존재 여부 — 로그인 응답 aiImageGenerated 판단용
    boolean existsByUserAndSelectedTrue(User user);
}

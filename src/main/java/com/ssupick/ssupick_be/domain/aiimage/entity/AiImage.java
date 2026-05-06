package com.ssupick.ssupick_be.domain.aiimage.entity;

import com.ssupick.ssupick_be.common.base.BaseEntity;
import com.ssupick.ssupick_be.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "ai_image")
@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 유저의 이미지인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 사용자가 업로드한 원본 사진 S3 key
    @Column(name = "original_image_url", length = 500, nullable = false)
    private String originalImageUrl;

    // Gemini가 생성한 동물의 숲 스타일 이미지 S3 key (PENDING 상태에서는 null)
    @Column(name = "generated_image_url", length = 500)
    private String generatedImageUrl;

    // 사용자가 이 이미지를 최종 프로필로 선택했는지 여부
    @Builder.Default
    @Column(name = "is_selected", nullable = false)
    private boolean selected = false;

    // 비동기 생성 상태 — PENDING / DONE / FAILED
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 10, nullable = false)
    private AiImageStatus status = AiImageStatus.PENDING;

    // 생성 완료 시 생성된 이미지 URL 저장 + 상태 DONE으로 변경
    public void markDone(String generatedImageUrl) {
        this.generatedImageUrl = generatedImageUrl;
        this.status = AiImageStatus.DONE;
    }

    // 생성 실패 시 상태 FAILED로 변경
    public void markFailed() {
        this.status = AiImageStatus.FAILED;
    }

    // 사용자가 이 이미지를 최종 프로필로 선택
    public void select() {
        this.selected = true;
    }

    // 이전에 선택된 이미지 선택 해제 (다른 이미지 선택 시)
    public void deselect() {
        this.selected = false;
    }
}

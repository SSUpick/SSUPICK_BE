package com.ssupick.ssupick_be.common.status;

import com.ssupick.ssupick_be.common.base.BaseStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum SuccessStatus implements BaseStatus {

    COMMON_SUCCESS_STATUS(HttpStatus.OK, "COM_200", "성공적으로 처리되었습니다."),

    /**
     * Auth
     */
    LOGIN_SUCCESS(HttpStatus.OK, "AUTH_200_1", "로그인에 성공했습니다."),
    REISSUE_SUCCESS(HttpStatus.OK, "AUTH_200_2", "토큰 재발급에 성공했습니다."),
    WITHDRAW_SUCCESS(HttpStatus.OK, "AUTH_200_3", "회원 탈퇴가 완료되었습니다."),
    LOGOUT_SUCCESS(HttpStatus.OK, "AUTH_200_4", "로그아웃이 완료되었습니다."),

    /**
     * User
     */
    GET_USER_PROFILE_SUCCESS(HttpStatus.OK, "USER_200_1", "프로필 조회에 성공했습니다."),
    GET_USER_TARGET_PROFILE_SUCCESS(HttpStatus.OK, "USER_200_2", "상대 프로필 조회에 성공했습니다."),
    REGISTER_USER_ONBOARDING_SUCCESS(HttpStatus.OK, "USER_200_3", "온보딩 등록에 성공했습니다."),
    GET_USER_CARD_LIST_SUCCESS(HttpStatus.OK, "USER_200_4", "유저 카드 리스트 조회에 성공했습니다."),
    UPDATE_USER_PROFILE_SUCCESS(HttpStatus.OK, "USER_200_5", "프로필 수정에 성공했습니다."),
    GET_PROFILE_VIEW_LIST_SUCCESS(HttpStatus.OK, "USER_200_6", "열람 목록 조회에 성공했습니다."),


    /**
     * OAuth
     */
    OAUTH_KAKAO_LOGIN_SUCCESS(HttpStatus.OK, "OAUTH_200_1", "카카오 로그인에 성공했습니다."),

    /**
     * AI Image
     */
    AI_IMAGE_GENERATE_SUCCESS(HttpStatus.OK, "AI_200_1", "AI 이미지 생성에 성공했습니다."),
    AI_IMAGE_LIST_SUCCESS(HttpStatus.OK, "AI_200_2", "AI 이미지 목록 조회에 성공했습니다."),
    AI_IMAGE_SELECT_SUCCESS(HttpStatus.OK, "AI_200_3", "프로필 이미지 확정에 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

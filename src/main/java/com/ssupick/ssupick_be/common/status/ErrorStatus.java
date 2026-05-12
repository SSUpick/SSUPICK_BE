package com.ssupick.ssupick_be.common.status;

import com.ssupick.ssupick_be.common.base.BaseStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseStatus {

    /**
     * Common
     */
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COM_400", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COM_401", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COM_403", "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COM_404", "요청한 자원을 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COM_405", "허용되지 않은 메소드입니다."),
    CONFLICT(HttpStatus.CONFLICT, "COM_409", "이미 존재하는 리소스입니다."),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "COM_415", "지원하지 않는 형식입니다."),
    UNPROCESSABLE_ENTITY(HttpStatus.UNPROCESSABLE_ENTITY, "COM_422", "처리할 수 없는 요청입니다."),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "COM_429", "요청이 너무 많습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COM_500", "서버 내부 오류입니다."),

    /**
     * JWT
     */
    JWT_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "JWT_401_1", "토큰이 존재하지 않습니다."),
    JWT_INVALID_SIGNATURE(HttpStatus.UNAUTHORIZED, "JWT_401_2", "잘못된 JWT 서명입니다."),
    JWT_MALFORMED(HttpStatus.UNAUTHORIZED, "JWT_401_3", "잘못된 JWT 형식입니다."),
    JWT_EXPIRED(HttpStatus.UNAUTHORIZED, "JWT_401_4", "만료된 JWT 토큰입니다."),
    JWT_UNSUPPORTED(HttpStatus.UNAUTHORIZED, "JWT_401_5", "지원되지 않는 JWT 토큰입니다."),
    JWT_INVALID(HttpStatus.UNAUTHORIZED, "JWT_401_6", "JWT 토큰이 잘못되었습니다."),
    JWT_EXTRACT_ID_FAILED(HttpStatus.UNAUTHORIZED, "JWT_401_7", "토큰에서 사용자 정보를 추출할 수 없습니다."),
    JWT_GENERAL_ERROR(HttpStatus.UNAUTHORIZED, "JWT_401_8", "JWT 토큰 처리 중 알 수 없는 오류가 발생했습니다."),
    JWT_INVALID_TYPE(HttpStatus.UNAUTHORIZED, "JWT_401_9", "토큰 타입이 유효하지 않습니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "JWT_401_10", "DB에 저장된 토큰과 일치하지 않습니다."),
    REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "JWT_401_11", "리프레시 토큰 정보가 사용자 정보와 일치하지 않습니다."),
    JWT_EXTRACT_ROLE_FAILED(HttpStatus.UNAUTHORIZED, "JWT_401_12", "토큰에서 사용자 Role을 추출할 수 없습니다."),

    /**
     * Kakao OAuth
     */
    KAKAO_TOKEN_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "KAKAO_502_1", "카카오 토큰 발급에 실패했습니다."),
    KAKAO_USER_INFO_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "KAKAO_502_2", "카카오 사용자 정보 조회에 실패했습니다."),
    KAKAO_UNLINK_FAILED(HttpStatus.BAD_GATEWAY, "KAKAO_502_3", "카카오 연동 해제에 실패했습니다."),

    /**
     * User
     */
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404_1", "사용자를 찾을 수 없습니다."),
    USER_ONBOARDING_INCOMPLETE(HttpStatus.FORBIDDEN, "USER_403_1", "온보딩을 완료하지 않은 유저입니다."),
    USER_PROFILE_INCOMPLETE(HttpStatus.FORBIDDEN, "USER_403_4", "프로필 등록을 완료하지 않은 유저입니다."),
    PROFILE_VIEW_COUPON_REQUIRED(HttpStatus.PAYMENT_REQUIRED, "USER_402_1", "프로필 조회 쿠폰이 부족합니다."),
    ONBOARDING_ALREADY_COMPLETED(HttpStatus.CONFLICT, "USER_409_1", "이미 온보딩을 완료한 유저입니다."),
    INVALID_APPEAL_CONTENT(HttpStatus.BAD_REQUEST, "USER_400_1", "어필 항목 내용이 올바르지 않습니다."),
    SELF_VIEW_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "USER_400_2", "자기 자신의 프로필은 열람할 수 없습니다."),
    NICKNAME_PROFANITY_DETECTED(HttpStatus.BAD_REQUEST, "USER_400_3", "닉네임에 사용할 수 없는 표현이 포함되어 있습니다."),
    APPEAL_PROFANITY_DETECTED(HttpStatus.BAD_REQUEST, "USER_400_4", "어필 항목에 사용할 수 없는 표현이 포함되어 있습니다."),
    NICKNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER_409_2", "이미 사용 중인 닉네임입니다."),
    NICKNAME_GENERATION_FAILED(HttpStatus.CONFLICT, "USER_409_3", "사용 가능한 랜덤 닉네임을 생성할 수 없습니다."),

    /**
     * AI Image
     */
    AI_IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "AI_404_1", "AI 이미지를 찾을 수 없습니다."),
    AI_IMAGE_GENERATION_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "AI_429_1", "이미지 생성 횟수를 모두 사용했습니다."),
    AI_IMAGE_NOT_OWNED(HttpStatus.FORBIDDEN, "AI_403_1", "해당 이미지에 대한 권한이 없습니다."),
    AI_IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "AI_500_1", "이미지 업로드에 실패했습니다."),
    AI_IMAGE_GENERATION_FAILED(HttpStatus.BAD_GATEWAY, "AI_502_1", "AI 이미지 생성에 실패했습니다."),

    /**
     * Payment
     */
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_404_1", "결제 정보를 찾을 수 없습니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "PAYMENT_400_1", "결제 금액이 일치하지 않습니다."),
    PAYMENT_STATUS_INVALID(HttpStatus.BAD_REQUEST, "PAYMENT_400_2", "결제가 완료되지 않았습니다."),
    PAYMENT_PHONE_NUMBER_REQUIRED(HttpStatus.BAD_REQUEST, "PAYMENT_400_3", "결제를 진행하려면 전화번호 등록이 필요합니다."),
    PAYMENT_ALREADY_PROCESSED(HttpStatus.CONFLICT, "PAYMENT_409_1", "이미 처리된 결제입니다."),
    PORTONE_REQUEST_FAILED(HttpStatus.BAD_GATEWAY, "PORTONE_502_1", "포트원 API 요청에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}

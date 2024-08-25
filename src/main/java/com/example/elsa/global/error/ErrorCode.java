package com.example.elsa.global.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    /*
     * 400 BAD_REQUEST: 잘못된 요청
     */
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),

    /*
     * 404 NOT_FOUND: 리소스를 찾을 수 없음
     */
    POSTS_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글 정보를 찾을 수 없습니다."),

    /*
     * 405 METHOD_NOT_ALLOWED: 허용되지 않은 Request Method 호출
     */
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 메서드입니다."),

    /*
     * 500 INTERNAL_SERVER_ERROR: 내부 서버 오류
     */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류입니다."),
    VALIDATION_FAILURE(HttpStatus.BAD_REQUEST, "유효성 검사에 실패했습니다."),
    INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, "요청 본문이 유효하지 않습니다."),


    LOGIN_FAILURE(HttpStatus.UNAUTHORIZED, "아이디나 비밀번호가 잘못되었습니다."),
    DUPLICATE_DATA(HttpStatus.CONFLICT, "같은 이름의 데이터가 존재합니다."),
    ALREADY_FRIEND(HttpStatus.CONFLICT, "이미 친구로 추가된 회원입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 가입된 이메일 정보입니다."),
    NOT_FRIEND(HttpStatus.BAD_REQUEST, "친구 관계가 아닙니다."),
    DUPLICATE_FRIEND_REQUEST(HttpStatus.CONFLICT, "이미 친구 요청을 보낸 상태입니다."),
    UNAUTHORIZED_TOKEN(HttpStatus.UNAUTHORIZED, "권한 정보가 없는 토큰입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 회원을 찾을 수 없습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 유효하지 않습니다."),
    NOT_FOUND_REFRESH_TOKEN(HttpStatus.NOT_FOUND, "리프레시 토큰을 찾지 못했습니다."),
    NOT_MATCHED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "DB의 리프레시 토큰값과 일치하지 않습니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료되었습니다."),
    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "엑세스 토큰이 만료되었습니다."),
    NO_AUTHORITY_FOUND(HttpStatus.NOT_FOUND, "회원의 권한 정보를 찾을 수 없습니다"),
    DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "해당하는 데이터를 찾을 수 없습니다."),
    FRIEND_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "친구 요청을 찾을 수 없습니다."),

    DIAGNOSIS_QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "자가진단 질문 항목을 찾을 수 없습니다.");


    private final HttpStatus status;
    private final String message;

}

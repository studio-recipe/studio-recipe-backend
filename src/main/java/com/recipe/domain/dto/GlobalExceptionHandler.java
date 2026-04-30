package com.recipe.domain.dto;
import com.recipe.exceptions.user.UserException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외 처리 클래스
 * 모든 controller에서 발생하는 예외를 여기서 처리
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 사용자 정의 예외(UserException) 처리
     */
    @ExceptionHandler(UserException.class)
    public ResponseEntity<Map<String, Object>> handleUserException(UserException ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());
        errorResponse.put("code", ex.getCode());

        HttpStatus status = (ex.getCode() == 404) ? HttpStatus.NOT_FOUND :
                (ex.getCode() == 409) ? HttpStatus.CONFLICT :
                        HttpStatus.BAD_REQUEST;

        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * 그 외 모든 일반 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "서버 내부 오류가 발생했습니다.");
        errorResponse.put("message", ex.getMessage());

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

package com.recipe.controller.advice;

import com.recipe.exceptions.recipe.RecipeException;
import com.recipe.exceptions.user.UserException;
import lombok.extern.log4j.Log4j2;
import org.apache.coyote.Response;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Log4j2
public class ControllerAdvice {
    @ExceptionHandler(RecipeException.class)
    public ResponseEntity<Map<String, String>>RecipeEx(RecipeException ex){
        HttpStatus status = HttpStatus.resolve(ex.getCode());
        if(status == null){
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        Map<String ,String> errors =Map.of("message", ex.getMessage());
        return ResponseEntity.status(status).body(errors);
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<Map<String, String>>UserEx(UserException ex){
        HttpStatus status =HttpStatus.resolve(ex.getCode());
        if (status == null) {
            //log
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        Map<String, String> errors = new HashMap<>();
        errors.put("message", ex.getMessage());
        return ResponseEntity.status(status).body(errors);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> MethodArgumentNotValidEx(MethodArgumentNotValidException ex){
        Map<String, Object> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach((fieldError) -> {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<?> handleIllegalStateEx(IllegalStateException ex){
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolationException(DataIntegrityViolationException ex){
        if(ex.getMessage() != null && ex.getMessage().contains("UQ_RECIPE_LIKE")){
            return new ResponseEntity<>("이미 좋아요를 눌렀습니다.", HttpStatus.CONFLICT); //409
        }
        return new ResponseEntity<>("데이터베이스 제약 조건 위반",  HttpStatus.SERVICE_UNAVAILABLE); //500
        }

        @ExceptionHandler(HttpMessageConversionException.class)
    public ResponseEntity<?> handleHttpMessageConversionException(HttpMessageConversionException ex){
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
}

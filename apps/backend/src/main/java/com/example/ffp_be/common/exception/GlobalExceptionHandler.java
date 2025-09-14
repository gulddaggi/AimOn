package com.example.ffp_be.common.exception;

import com.example.ffp_be.common.dto.ErrorResponse;
import com.example.ffp_be.user.exception.AuthenticationException;
import com.example.ffp_be.user.exception.DuplicatedNicknameException;
import com.example.ffp_be.user.exception.InvalidLevelException;
import com.example.ffp_be.user.exception.UserProfileNotFoundException;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ErrorResponse buildErrorResponse(HttpStatus status, String code, String message,
        Map<String, String> errors, HttpServletRequest request) {
        return ErrorResponse.builder()
            .status(status.value())
            .code(code)
            .message(message)
            .timestamp(LocalDateTime.now())
            .errors(errors)
            .path(request.getRequestURI())
            .method(request.getMethod())
            .build();
    }

    // CustomException 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustom(CustomException ex, HttpServletRequest request) {
        log.warn("CustomException 발생: {} - {}", ex.getCode(), ex.getMessage());
        return ResponseEntity.status(ex.getStatus())
            .body(buildErrorResponse(ex.getStatus(), ex.getCode(), ex.getMessage(), null, request));
    }

    // Validation 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, 
        HttpServletRequest request) {
        Map<String, String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                FieldError::getDefaultMessage,
                (existing, replacement) -> existing + "; " + replacement
            ));
        
        log.warn("Validation 실패: {} - {}", request.getRequestURI(), errors);
        
        return ResponseEntity.badRequest()
            .body(buildErrorResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", 
                "입력값 검증에 실패했습니다.", errors, request));
    }

    // 필수 파라미터 누락 예외 처리
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex,
        HttpServletRequest request) {
        log.warn("필수 파라미터 누락: {} - {}", ex.getParameterName(), request.getRequestURI());
        
        return ResponseEntity.badRequest()
            .body(buildErrorResponse(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER",
                String.format("필수 파라미터 '%s'가 누락되었습니다.", ex.getParameterName()), null, request));
    }

    // 파라미터 타입 불일치 예외 처리
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
        HttpServletRequest request) {
        log.warn("파라미터 타입 불일치: {} - {}", ex.getName(), request.getRequestURI());
        
        return ResponseEntity.badRequest()
            .body(buildErrorResponse(HttpStatus.BAD_REQUEST, "TYPE_MISMATCH",
                String.format("파라미터 '%s'의 타입이 올바르지 않습니다.", ex.getName()), null, request));
    }

    // 404 에러 처리
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFound(NoHandlerFoundException ex,
        HttpServletRequest request) {
        log.warn("핸들러를 찾을 수 없음: {} {}", ex.getHttpMethod(), ex.getRequestURL());
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(buildErrorResponse(HttpStatus.NOT_FOUND, "NOT_FOUND",
                "요청한 리소스를 찾을 수 없습니다.", null, request));
    }

    // IllegalStateException 처리
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex,
        HttpServletRequest request) {
        log.warn("IllegalStateException: {} - {}", ex.getMessage(), request.getRequestURI());
        
        return ResponseEntity.badRequest()
            .body(buildErrorResponse(HttpStatus.BAD_REQUEST, "ILLEGAL_STATE", ex.getMessage(), null, request));
    }

    // IllegalArgumentException 처리
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex,
        HttpServletRequest request) {
        log.warn("IllegalArgumentException: {} - {}", ex.getMessage(), request.getRequestURI());
        
        return ResponseEntity.badRequest()
            .body(buildErrorResponse(HttpStatus.BAD_REQUEST, "ILLEGAL_ARGUMENT", ex.getMessage(), null, request));
    }

    // 예상치 못한 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("예상치 못한 오류 발생: {} - {}", request.getRequestURI(), ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",
                "예상치 못한 오류가 발생했습니다.", null, request));
    }
}



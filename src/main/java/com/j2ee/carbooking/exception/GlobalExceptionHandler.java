package com.j2ee.carbooking.exception;

import com.j2ee.carbooking.dto.response.AppApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 1. Xử lý lỗi logic nghiệp vụ
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<AppApiResponse<Object>> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.badRequest()
            .body(AppApiResponse.error(e.getMessage()));
    }

    // 2. Xử lý lỗi Validation (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AppApiResponse<Object>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
        
        return ResponseEntity.badRequest()
            .body(AppApiResponse.error("Lỗi dữ liệu: " + message));
    }

    // 3. Xử lý lỗi chung
    @ExceptionHandler(Exception.class)
    public ResponseEntity<AppApiResponse<Object>> handleGeneralException(Exception e) {
        return ResponseEntity.internalServerError()
            .body(AppApiResponse.error("Lỗi hệ thống: " + e.getMessage()));
    }
}

package com.j2ee.carbooking.exception;

import com.j2ee.carbooking.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException e) {
        // Trả về lỗi 400 kèm message từ RuntimeException
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(e.getMessage()));
    }

    // Bạn có thể thêm các xử lý cho các Exception khác ở đây
    // Ví dụ: MethodArgumentNotValidException (lỗi @Valid)
}

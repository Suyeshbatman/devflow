package com.devflow.user.exception;

import com.devflow.common.dto.ApiResponse;
import com.devflow.common.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

// @RestControllerAdvice = intercepts exceptions thrown from ANY
//   @RestController in this service
//   It's a global try-catch for the entire HTTP layer
// @Slf4j = Lombok gives us 'log' for logging
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ═══════════════════════════════════════════════════════
    // Handles our custom BaseException and all subclasses
    // e.g. when AuthService throws:
    //   throw new BaseException(ErrorCode.USER_ALREADY_EXISTS)
    // This method catches it and returns:
    //   HTTP 409 { "success": false, "message": "A user with
    //              this email already exists" }
    // ═══════════════════════════════════════════════════════
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Void>> handleBaseException(
            BaseException ex) {
        log.error("Business exception: {}", ex.getMessage());

        // Get the HTTP status code from the ErrorCode enum
        HttpStatus status = HttpStatus.valueOf(
                ex.getErrorCode().getHttpStatus());

        return ResponseEntity
                .status(status)
                .body(ApiResponse.error(ex.getMessage()));
    }

    // ═══════════════════════════════════════════════════════
    // Handles @Valid validation failures
    // e.g. client sends { "email": "not-an-email" }
    // Spring throws MethodArgumentNotValidException
    // We catch it and return all field errors clearly:
    // { "email": "Must be a valid email address",
    //   "password": "Must be at least 8 characters" }
    // ═══════════════════════════════════════════════════════
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>>
    handleValidationException(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        // getBindingResult().getAllErrors() = list of all failed validations
        // We cast each to FieldError to get the field name
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Validation failed: {}", errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Map<String, String>>builder()
                        .success(false)
                        .message("Validation failed")
                        .data(errors)
                        .build());
    }

    // ═══════════════════════════════════════════════════════
    // Catch-all for any unexpected exception
    // Prevents stack traces leaking to the client
    // ═══════════════════════════════════════════════════════
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex) {
        log.error("Unexpected error: ", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(
                        "An unexpected error occurred. Please try again."));
    }
}
package com.application.bhyt.exception;

import com.application.bhyt.dto.response.MyApiResponse;
import com.application.bhyt.enums.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<MyApiResponse> handlingRuntimeException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        return ResponseEntity.badRequest().body(MyApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage()).build());
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    ResponseEntity<MyApiResponse> handlingIllegalArgumentException(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(MyApiResponse.builder()
                .code(400)
                .message(exception.getMessage())
                .build());
    }

}

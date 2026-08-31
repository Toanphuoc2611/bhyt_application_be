package com.application.bhyt.exception;

import com.application.bhyt.enums.ErrorCode;
import lombok.Getter;

/**
 * Ngoại lệ nghiệp vụ. Luôn gắn với một {@link ErrorCode} để
 * {@link GlobalExceptionHandler} ánh xạ ra HTTP status và body chuẩn.
 */
@Getter
public class MyException extends RuntimeException {

    private final ErrorCode errorCode;

    public MyException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /** Kèm thông tin bổ sung, ví dụ "ID: 5". */
    public MyException(ErrorCode errorCode, String chiTiet) {
        super(errorCode.getMessage() + " (" + chiTiet + ")");
        this.errorCode = errorCode;
    }
}

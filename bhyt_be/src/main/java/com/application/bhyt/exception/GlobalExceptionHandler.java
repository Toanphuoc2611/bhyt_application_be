package com.application.bhyt.exception;

import com.application.bhyt.dto.response.ApiResponse;
import com.application.bhyt.enums.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.util.stream.Collectors;

/**
 * Chuyển mọi ngoại lệ thành response chuẩn {@link ApiResponse} với HTTP status phù hợp.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Lỗi nghiệp vụ đã biết. */
    @ExceptionHandler(MyException.class)
    public ResponseEntity<ApiResponse<Void>> xuLyMyException(MyException ex) {
        ErrorCode ec = ex.getErrorCode();
        log.warn("Lỗi nghiệp vụ [{}]: {}", ec.getCode(), ex.getMessage());
        return ResponseEntity.status(ec.getHttpStatus())
                .body(ApiResponse.loi(ec.getCode(), ex.getMessage()));
    }

    /** Lỗi validate dữ liệu request (@NotNull, @Positive...). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> xuLyValidation(MethodArgumentNotValidException ex) {
        String chiTiet = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fieldMessage(fe))
                .collect(Collectors.joining("; "));
        log.warn("Lỗi validate dữ liệu: {}", chiTiet);
        return ResponseEntity.status(ErrorCode.LOI_KIEM_TRA_DU_LIEU.getHttpStatus())
                .body(ApiResponse.loi(ErrorCode.LOI_KIEM_TRA_DU_LIEU.getCode(),
                        ErrorCode.LOI_KIEM_TRA_DU_LIEU.getMessage() + ": " + chiTiet));
    }

    /** Body JSON sai định dạng / tham số query thiếu hoặc sai kiểu. */
    @ExceptionHandler({HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResponse<Void>> xuLyRequestSai(Exception ex) {
        log.warn("Request không hợp lệ: {}", ex.getMessage());
        return ResponseEntity.status(ErrorCode.YEU_CAU_KHONG_HOP_LE.getHttpStatus())
                .body(ApiResponse.loi(ErrorCode.YEU_CAU_KHONG_HOP_LE.getCode(),
                        ErrorCode.YEU_CAU_KHONG_HOP_LE.getMessage()));
    }

    /** Mọi lỗi còn lại. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> xuLyChung(Exception ex) {
        log.error("Lỗi không mong muốn", ex);
        return ResponseEntity.status(ErrorCode.LOI_HE_THONG.getHttpStatus())
                .body(ApiResponse.loi(ErrorCode.LOI_HE_THONG.getCode(), ErrorCode.LOI_HE_THONG.getMessage()));
    }

    private String fieldMessage(FieldError fe) {
        return fe.getField() + " " + (fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "không hợp lệ");
    }
}

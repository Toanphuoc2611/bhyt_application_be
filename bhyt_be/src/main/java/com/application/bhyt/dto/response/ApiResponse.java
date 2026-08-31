package com.application.bhyt.dto.response;

import com.application.bhyt.enums.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Vỏ bọc chuẩn cho mọi response REST.
 *
 * <pre>
 * {
 *   "code": "00000",       // mã kết quả (00000 = thành công)
 *   "message": "Thành công",
 *   "data": { ... }         // dữ liệu trả về, có thể là {@link PageResponse} với danh sách
 * }
 * </pre>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /** Mã kết quả. */
    private String code;

    /** Thông điệp mô tả (tiếng Việt). */
    private String message;

    /** Dữ liệu trả về. */
    private T data;

    /** Tạo response thành công kèm dữ liệu. */
    public static <T> ApiResponse<T> thanhCong(T data) {
        return new ApiResponse<>(ErrorCode.THANH_CONG.getCode(), ErrorCode.THANH_CONG.getMessage(), data);
    }

    /** Tạo response thành công không kèm dữ liệu. */
    public static ApiResponse<Void> thanhCong() {
        return new ApiResponse<>(ErrorCode.THANH_CONG.getCode(), ErrorCode.THANH_CONG.getMessage(), null);
    }

    /** Tạo response lỗi. */
    public static ApiResponse<Void> loi(String code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}

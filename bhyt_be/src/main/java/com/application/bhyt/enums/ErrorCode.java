package com.application.bhyt.enums;

import org.springframework.http.HttpStatus;

/**
 * Mã lỗi nghiệp vụ dùng thống nhất trong toàn hệ thống.
 * Mỗi mã gồm: mã chuỗi, thông điệp tiếng Việt và HTTP status tương ứng.
 */
public enum ErrorCode {

    THANH_CONG("00000", "Thành công", HttpStatus.OK),

    YEU_CAU_KHONG_HOP_LE("40000", "Yêu cầu không hợp lệ", HttpStatus.BAD_REQUEST),
    LOI_KIEM_TRA_DU_LIEU("40010", "Dữ liệu không hợp lệ", HttpStatus.BAD_REQUEST),
    KHOANG_NGAY_KHONG_HOP_LE("40011", "Khoảng ngày không hợp lệ", HttpStatus.BAD_REQUEST),

    KHACH_HANG_KHONG_TON_TAI("40401", "Không tìm thấy khách hàng", HttpStatus.NOT_FOUND),
    KHACH_HANG_DA_XOA("40402", "Khách hàng đã bị xóa", HttpStatus.BAD_REQUEST),
    CCCD_DA_TON_TAI("40901", "CCCD đã tồn tại", HttpStatus.CONFLICT),

    BHYT_KHONG_TON_TAI("40403", "Không tìm thấy thẻ BHYT", HttpStatus.NOT_FOUND),
    BHYT_DA_XOA("40404", "Thẻ BHYT đã bị xóa", HttpStatus.BAD_REQUEST),

    HO_GIA_DINH_KHONG_TON_TAI("40405", "Không tìm thấy hộ gia đình", HttpStatus.NOT_FOUND),
    THANH_VIEN_KHONG_TON_TAI("40406", "Không tìm thấy thành viên trong hộ", HttpStatus.NOT_FOUND),
    KHACH_HANG_DA_CO_HO("40902", "Khách hàng đã thuộc một hộ gia đình khác", HttpStatus.CONFLICT),

    LOI_XUAT_FILE("50002", "Lỗi khi xuất file Excel", HttpStatus.INTERNAL_SERVER_ERROR),
    LOI_HE_THONG("50001", "Lỗi hệ thống", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}

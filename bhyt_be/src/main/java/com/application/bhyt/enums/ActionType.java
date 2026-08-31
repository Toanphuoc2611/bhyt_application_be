package com.application.bhyt.enums;

/**
 * Loại hành động ghi vào nhật ký thao tác (audit log).
 */
public enum ActionType {
    /** Tạo khách hàng. */
    TAO_KHACH_HANG,
    /** Cập nhật khách hàng. */
    CAP_NHAT_KHACH_HANG,
    /** Xóa mềm khách hàng. */
    XOA_KHACH_HANG,

    /** Tạo mới thẻ BHYT. */
    TAO_BHYT,
    /** Cập nhật thẻ BHYT. */
    CAP_NHAT_BHYT,
    /** Gia hạn thẻ BHYT. */
    GIA_HAN_BHYT,
    /** Xóa mềm thẻ BHYT. */
    XOA_BHYT,
    /** Xác nhận đã nhận hoa hồng. */
    XAC_NHAN_HOA_HONG,

    /** Tạo hộ gia đình. */
    TAO_HO_GIA_DINH,
    /** Cập nhật hộ gia đình. */
    CAP_NHAT_HO_GIA_DINH,
    /** Thêm thành viên vào hộ. */
    THEM_THANH_VIEN_HO,
    /** Đưa thành viên ra khỏi hộ. */
    XOA_THANH_VIEN_HO,

    /** Sao lưu toàn bộ dữ liệu ra Excel. */
    SAO_LUU_EXCEL
}

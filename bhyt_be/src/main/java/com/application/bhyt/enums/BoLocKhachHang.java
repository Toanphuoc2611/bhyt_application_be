package com.application.bhyt.enums;

/**
 * Bộ lọc khách hàng dùng ở Trang 3 (quản lý hộ).
 * Giá trị gửi lên từ FE qua tham số {@code filter}.
 */
public enum BoLocKhachHang {

    /** Tất cả khách hàng (mặc định). */
    TAT_CA,

    /** Khách hàng không thuộc hộ gia đình nào (không có dòng thành viên đang hoạt động). */
    KHONG_CO_HO,

    /** Khách hàng chưa mua BHYT (không có dòng {@code bao_hiem_y_te} nào chưa xóa). */
    CHUA_MUA_BHYT;

    /** Chuyển chuỗi -> enum, không phân biệt hoa thường; null/không hợp lệ -> {@link #TAT_CA}. */
    public static BoLocKhachHang tuChuoi(String value) {
        if (value == null || value.isBlank()) {
            return TAT_CA;
        }
        for (BoLocKhachHang b : values()) {
            if (b.name().equalsIgnoreCase(value.trim())) {
                return b;
            }
        }
        return TAT_CA;
    }
}

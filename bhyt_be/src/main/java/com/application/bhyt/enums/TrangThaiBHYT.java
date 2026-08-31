package com.application.bhyt.enums;

/**
 * Bộ lọc trạng thái thẻ BHYT dùng ở Trang 2 (quản lý BHYT).
 * Giá trị gửi lên từ FE qua tham số {@code trangThai}.
 */
public enum TrangThaiBHYT {

    /** Không lọc theo trạng thái (mặc định). */
    TAT_CA,

    /** Đã hết hạn: {@code han_the < hôm nay}. */
    HET_HAN,

    /** Gần hết hạn trong 7 ngày tới. */
    GAN_HET_HAN_7,

    /** Gần hết hạn trong 14 ngày tới. */
    GAN_HET_HAN_14,

    /** Gần hết hạn trong 30 ngày tới. */
    GAN_HET_HAN_30,

    /** Gần hết hạn trong 60 ngày tới. */
    GAN_HET_HAN_60,

    /** Chưa nhận hoa hồng: {@code da_nhan_hoa_hong = 0}. */
    CHUA_NHAN_HOA_HONG;

    /** Chuyển chuỗi -> enum, không phân biệt hoa thường; null/không hợp lệ -> {@link #TAT_CA}. */
    public static TrangThaiBHYT tuChuoi(String value) {
        if (value == null || value.isBlank()) {
            return TAT_CA;
        }
        for (TrangThaiBHYT t : values()) {
            if (t.name().equalsIgnoreCase(value.trim())) {
                return t;
            }
        }
        return TAT_CA;
    }

    /** Số ngày của cửa sổ "gần hết hạn"; trả về -1 nếu không phải loại lọc theo ngày. */
    public int soNgayGanHetHan() {
        return switch (this) {
            case GAN_HET_HAN_7 -> 7;
            case GAN_HET_HAN_14 -> 14;
            case GAN_HET_HAN_30 -> 30;
            case GAN_HET_HAN_60 -> 60;
            default -> -1;
        };
    }
}

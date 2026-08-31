package com.application.bhyt.enums;

/**
 * Loại thẻ BHYT. Giá trị lưu trong DB là chuỗi tiếng Việt ("mới" / "gia hạn")
 * theo đúng quy ước của client.
 */
public enum LoaiBHYT {

    /** Mua mới. */
    MOI("mới"),

    /** Gia hạn thẻ đã có. */
    GIA_HAN("gia hạn");

    private final String giaTri;

    LoaiBHYT(String giaTri) {
        this.giaTri = giaTri;
    }

    /** Chuỗi lưu trong cột {@code loai}. */
    public String getGiaTri() {
        return giaTri;
    }

    /** Chuyển chuỗi -> enum; null/không hợp lệ -> {@link #MOI}. */
    public static LoaiBHYT tuChuoi(String value) {
        if (value == null) {
            return MOI;
        }
        for (LoaiBHYT loai : values()) {
            if (loai.giaTri.equalsIgnoreCase(value.trim())) {
                return loai;
            }
        }
        return MOI;
    }
}

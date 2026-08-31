package com.application.bhyt.util;

import com.application.bhyt.enums.ErrorCode;
import com.application.bhyt.exception.MyException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Tiện ích chuyển đổi ngày giữa chuỗi và {@link LocalDate}.
 *
 * <p>Quy ước của dự án: FE gửi và nhận ngày dưới dạng chuỗi {@code dd/MM/yyyy}
 * (ví dụ {@code 05/09/2026}). Cũng chấp nhận thêm định dạng ISO {@code yyyy-MM-dd}
 * để linh hoạt khi test bằng curl.</p>
 */
public final class DateUtils {

    private static final DateTimeFormatter DINH_DANG_VN = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private DateUtils() {
    }

    /**
     * Chuyển chuỗi -> {@link LocalDate}. Trả về {@code null} nếu chuỗi rỗng/null.
     *
     * @throws MyException nếu chuỗi có nội dung nhưng sai định dạng
     */
    public static LocalDate parse(String chuoiNgay) {
        if (chuoiNgay == null || chuoiNgay.isBlank()) {
            return null;
        }
        String value = chuoiNgay.trim();
        try {
            return LocalDate.parse(value, DINH_DANG_VN);
        } catch (DateTimeParseException boQua) {
            // thử tiếp định dạng ISO
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ex) {
            throw new MyException(ErrorCode.LOI_KIEM_TRA_DU_LIEU,
                    "Sai định dạng ngày '" + chuoiNgay + "', cần dd/MM/yyyy");
        }
    }

    /** Chuyển {@link LocalDate} -> chuỗi {@code dd/MM/yyyy}. Trả về {@code null} nếu ngày null. */
    public static String format(LocalDate ngay) {
        return ngay == null ? null : ngay.format(DINH_DANG_VN);
    }
}

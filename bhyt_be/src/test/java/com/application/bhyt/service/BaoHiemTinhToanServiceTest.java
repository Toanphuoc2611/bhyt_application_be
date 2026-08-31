package com.application.bhyt.service;

import com.application.bhyt.config.BhytProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Kiểm thử công thức tính tiền / hạn thẻ theo đúng file Excel "DS Mua BHYT"
 * (lương cơ sở 2.530.000, +1 tháng cho thẻ mới gói mặc định, +29 ngày cho gói tùy chọn,
 * ngưỡng mua sớm khi gia hạn 31 ngày).
 */
class BaoHiemTinhToanServiceTest {

    private final BaoHiemTinhToanService service = new BaoHiemTinhToanService(new BhytProperties());

    // ---------------- Số tiền thu ----------------

    @ParameterizedTest(name = "bậc {0}, {1} tháng -> {2} đ")
    @CsvSource({
            "1, 12, 1366200",   // 1.00 * 4.5% * 2.530.000 * 12
            "2, 12, 956340",    // 0.70 * ...
            "3, 12, 819720",    // 0.60 * ...
            "4, 12, 683100",    // 0.50 * ...
            "5, 12, 546480",    // 0.40 * ...
            "9, 12, 546480",    // bậc >= 5 -> 40%
            "2, 6, 478170"
    })
    void soTienThu_theoBac_khongMstb(int bac, int soThang, int mongDoi) {
        assertEquals(mongDoi, service.tinhSoTienThu(false, bac, soThang));
    }

    @Test
    void soTienThu_coMstb_luonLa40PhanTram() {
        assertEquals(546480, service.tinhSoTienThu(true, 1, 12));
        assertEquals(546480, service.tinhSoTienThu(true, 3, 12));
    }

    // ---------------- Hạn thẻ: thẻ mới ----------------

    @Test
    void hanThe_theMoi_goiMacDinh_congThem1Thang() {
        // 12 + 1 = 13 tháng
        LocalDate han = service.tinhHanThe("mới", LocalDate.of(2026, 1, 14), null, true, 12);
        assertEquals(LocalDate.of(2027, 2, 14), han);
    }

    @Test
    void hanThe_theMoi_goiTuyChon_congThem29Ngay() {
        LocalDate han = service.tinhHanThe("mới", LocalDate.of(2026, 1, 14), null, false, 6);
        assertEquals(LocalDate.of(2026, 7, 14).plusDays(29), han); // 2026-08-12
    }

    // ---------------- Hạn thẻ: gia hạn ----------------

    @Test
    void hanThe_giaHan_muaTrongNguong_tinhTuHanCu() {
        LocalDate hanCu = LocalDate.of(2026, 2, 14);
        LocalDate han = service.tinhHanThe("gia hạn", LocalDate.of(2026, 1, 20), hanCu, true, 12);
        assertEquals(LocalDate.of(2027, 2, 14), han);
    }

    @Test
    void hanThe_giaHan_muaQuaSom_giuNguyenHanCu() {
        LocalDate hanCu = LocalDate.of(2026, 2, 14);
        // mua 2025-12-01, sớm hơn hạn cũ 75 ngày (> 31) -> giữ nguyên
        LocalDate han = service.tinhHanThe("gia hạn", LocalDate.of(2025, 12, 1), hanCu, true, 12);
        assertEquals(hanCu, han);
    }
}

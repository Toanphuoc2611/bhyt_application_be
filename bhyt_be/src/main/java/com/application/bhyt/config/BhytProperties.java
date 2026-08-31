package com.application.bhyt.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Toàn bộ tham số nghiệp vụ, đọc từ file cấu hình (tiền tố {@code bhyt}).
 *
 * <p>Thứ tự ưu tiên: file <b>{@code bhyt-config.properties}</b> đặt CẠNH file .exe/.jar
 * (khai báo {@code spring.config.import} trong application.properties) &gt; giá trị
 * mặc định trong {@code application.properties} đóng gói sẵn &gt; giá trị mặc định
 * trong lớp này.</p>
 *
 * <p>Mục đích: khi build thành .exe, người dùng chỉ cần sửa file text cạnh .exe là
 * đổi được cách tính tiền / hạn thẻ, KHÔNG phải build lại.</p>
 */
@Component
@ConfigurationProperties(prefix = "bhyt")
@Data
public class BhytProperties {

    /** Lương cơ sở (t) - VNĐ. File Excel của khách hàng đang dùng 2.530.000. */
    private long luongCoSo = 2_530_000L;

    /** Mức đóng (m) - tỷ lệ thập phân. Mặc định 0.045 (4,5%). */
    private double mucDong = 0.045;

    /** Số tháng áp dụng khi ô "Tháng mua" để trống. Mặc định 12. */
    private int soThangMacDinh = 12;

    /**
     * Tỷ lệ đóng theo bậc "Người thứ" trong hộ: phần tử 0 = bậc 1, ... phần tử cuối
     * áp cho mọi bậc lớn hơn hoặc bằng kích thước danh sách.
     * Mặc định: 100%, 70%, 60%, 50%, 40%.
     */
    private List<Double> tyLeTheoBac = List.of(1.0, 0.7, 0.6, 0.5, 0.4);

    /** Người có mstb: luôn áp tỷ lệ này, bỏ qua bậc. Mặc định 40%. */
    private double tyLeMstb = 0.40;

    /**
     * Thẻ MỚI, gói mặc định (ô "Tháng mua" trống): hạn thẻ = ngày mua +
     * (soThangMacDinh + số này) tháng. File Excel dùng +1 (tức EDATE(ngày mua, 13)).
     */
    private int theMoiCongThemThang = 1;

    /**
     * Thẻ MỚI, gói tùy chọn "N tháng": hạn thẻ = ngày mua + N tháng + (số này) ngày.
     * File Excel dùng +29.
     */
    private int theMoiCongThemNgay = 29;

    /**
     * Gia hạn: nếu (ngày mua − ngày hết hạn cũ) &lt; −(số này) ngày, tức mua quá sớm,
     * thì giữ nguyên hạn cũ, không cộng thêm tháng. File Excel dùng 31.
     */
    private int giaHanNguongNgayMuaSom = 31;

    /** Cấu hình sao lưu Excel khi khởi động. */
    private Backup backup = new Backup();

    @Data
    public static class Backup {
        private boolean enabled = true;
        private String dir = "backup";
    }

    // ---- Tiện ích ----

    /** Tỷ lệ đóng cho một bậc (bậc tính từ 1). */
    public double tyLeChoBac(int bac) {
        int idx = Math.min(Math.max(bac, 1), tyLeTheoBac.size()) - 1;
        return tyLeTheoBac.get(idx);
    }
}

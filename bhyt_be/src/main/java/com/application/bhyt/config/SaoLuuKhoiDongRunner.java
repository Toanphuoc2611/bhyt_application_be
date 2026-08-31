package com.application.bhyt.config;

import com.application.bhyt.service.SaoLuuExcelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Mỗi lần server khởi động xong sẽ tự sao lưu toàn bộ dữ liệu ra file Excel.
 *
 * <p>Nếu sao lưu lỗi (ví dụ ổ đĩa đầy, không có quyền ghi): ghi log lỗi thật rõ và
 * in ra {@code System.err} để người vận hành thấy, NHƯNG server vẫn chạy bình
 * thường - sao lưu chỉ là chức năng phụ trợ, không được chặn ứng dụng.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SaoLuuKhoiDongRunner {

    private final BhytProperties properties;
    private final SaoLuuExcelService saoLuuExcelService;

    @EventListener(ApplicationReadyEvent.class)
    public void saoLuuKhiKhoiDong() {
        if (!properties.getBackup().isEnabled()) {
            log.info("Bỏ qua sao lưu khi khởi động (bhyt.backup.enabled=false)");
            return;
        }
        try {
            saoLuuExcelService.saoLuu();
        } catch (Exception ex) {
            String thongBao = "SAO LƯU DỮ LIỆU KHI KHỞI ĐỘNG THẤT BẠI: " + ex.getMessage()
                    + ". Server vẫn tiếp tục chạy, nhưng dữ liệu chưa được sao lưu lần này.";
            log.error(thongBao, ex);
            System.err.println("[BHYT] " + thongBao);
        }
    }
}

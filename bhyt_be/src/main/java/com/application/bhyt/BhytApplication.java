package com.application.bhyt;

import com.application.bhyt.dto.response.NhapExcelKetQua;
import com.application.bhyt.service.NhapExcelService;
import com.application.bhyt.service.SaoLuuExcelService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Điểm khởi động.
 *
 * <p>Chạy bình thường: mở REST server ở cổng 8080.</p>
 *
 * <p>Chế độ NHẬP DỮ LIỆU (chạy tay một lần để seed từ file Excel):</p>
 * <pre>
 *   java -jar bhyt.jar --nhap-excel="C:\đường dẫn\DS Mua BHYT.xlsm"
 *   (hoặc)  bhyt.exe --nhap-excel="C:\đường dẫn\DS Mua BHYT.xlsm"
 * </pre>
 * Chế độ này KHÔNG mở web server: xóa sạch 4 bảng, nạp lại từ file, sao lưu, rồi thoát.
 */
@SpringBootApplication
public class BhytApplication {

    public static void main(String[] args) {
        Optional<String> duongDanExcel = layThamSo(args, "nhap-excel");

        if (duongDanExcel.isPresent()) {
            chayNhapExcel(args, Path.of(duongDanExcel.get()));
            return;
        }

        SpringApplication.run(BhytApplication.class, args);
    }

    /** Chế độ CLI: nhập Excel rồi thoát, không bật Tomcat, không tự sao lưu lúc khởi động. */
    private static void chayNhapExcel(String[] args, Path file) {
        ConfigurableApplicationContext ctx = new SpringApplicationBuilder(BhytApplication.class)
                .web(WebApplicationType.NONE)
                .properties("bhyt.backup.enabled=false")
                .run(args);

        int[] ma = {0};
        try {
            NhapExcelKetQua kq = ctx.getBean(NhapExcelService.class).nhapTuFile(file);
            System.out.println(kq.tomTat());
            // Sao lưu ngay sau khi nhập để có bản Excel của dữ liệu vừa nạp
            try {
                Path bk = ctx.getBean(SaoLuuExcelService.class).saoLuu();
                System.out.println("Đã sao lưu: " + bk.toAbsolutePath());
            } catch (Exception e) {
                System.err.println("Sao lưu sau khi nhập thất bại (bỏ qua): " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("NHẬP EXCEL THẤT BẠI: " + e.getMessage());
            e.printStackTrace();
            ma[0] = 1;
        }

        int exit = SpringApplication.exit(ctx, () -> ma[0]);
        System.exit(exit);
    }

    /** Đọc tham số dạng {@code --ten=giá trị} hoặc {@code --ten "giá trị"}. */
    private static Optional<String> layThamSo(String[] args, String ten) {
        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            if (a.equals("--" + ten) && i + 1 < args.length) {
                return Optional.of(args[i + 1]);
            }
            if (a.startsWith("--" + ten + "=")) {
                return Optional.of(a.substring(ten.length() + 3));
            }
        }
        return Optional.empty();
    }
}

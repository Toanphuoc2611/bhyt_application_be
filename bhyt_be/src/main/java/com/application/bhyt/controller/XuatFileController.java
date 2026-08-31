package com.application.bhyt.controller;

import com.application.bhyt.dto.response.ApiResponse;
import com.application.bhyt.dto.response.XuatFileChoXuatDto;
import com.application.bhyt.service.XuatFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API cấp dữ liệu cho Trang 4 (Xuất file Excel).
 * Base path {@code /api/v1/xuat-file}.
 *
 * <p>Việc sinh file .xlsx chọn lọc do FE làm; backend chỉ trả dữ liệu.
 * Chức năng sao lưu toàn bộ ra Excel chạy tự động khi khởi động server
 * (không có endpoint - xem {@code SaoLuuExcelService}).</p>
 */
@RestController
@RequestMapping("/api/v1/xuat-file")
@RequiredArgsConstructor
public class XuatFileController {

    private final XuatFileService xuatFileService;

    /**
     * Dữ liệu cho bảng Trang 4 (đúng thứ tự cột client yêu cầu) + bảng "ds hộ".
     * Không theo dõi trạng thái "đã xuất" / "hoàn tác".
     */
    @GetMapping("/cho-xuat")
    public ApiResponse<XuatFileChoXuatDto> choXuat() {
        return ApiResponse.thanhCong(xuatFileService.layDuLieuChoXuat());
    }
}

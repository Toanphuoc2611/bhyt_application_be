package com.application.bhyt.controller;

import com.application.bhyt.dto.response.ApiResponse;
import com.application.bhyt.dto.response.ThongKeDoanhThuDto;
import com.application.bhyt.dto.response.ThongKeTongQuanDto;
import com.application.bhyt.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API thống kê - Trang 1. Base path {@code /api/v1/thong-ke}.
 */
@RestController
@RequestMapping("/api/v1/thong-ke")
@RequiredArgsConstructor
public class ThongKeController {

    private final DashboardService dashboardService;

    /** 5 chỉ số đếm của dashboard trong một response. */
    @GetMapping("/tong-quan")
    public ApiResponse<ThongKeTongQuanDto> tongQuan() {
        return ApiResponse.thanhCong(dashboardService.tongQuan());
    }

    /** Doanh thu + số thẻ mới/gia hạn theo khoảng ngày (dd/MM/yyyy). */
    @GetMapping("/doanh-thu")
    public ApiResponse<ThongKeDoanhThuDto> doanhThu(@RequestParam String tuNgay,
                                                    @RequestParam String denNgay) {
        return ApiResponse.thanhCong(dashboardService.doanhThu(tuNgay, denNgay));
    }
}

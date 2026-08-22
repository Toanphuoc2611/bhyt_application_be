package com.application.bhyt.controller;

import com.application.bhyt.dto.response.MyApiResponse;
import com.application.bhyt.dto.response.ThongKeChungResponse;
import com.application.bhyt.service.BHYTService;
import com.application.bhyt.service.HoGiaDinhService;
import com.application.bhyt.service.KhachHangService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/thong-ke")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ThongKeController {

    BHYTService bhytService;
    KhachHangService khachHangService;
    HoGiaDinhService hoGiaDinhService;

    @GetMapping("/chung")
    public MyApiResponse<ThongKeChungResponse> getThongKeChung() {
        int tongSoKhachHang = khachHangService.countSoKhachHang();
        int tongSoKhachHangChuaCoHo = khachHangService.countKhachHangChuaCoHo();
        int soHoGiaDinh = hoGiaDinhService.countHoGiaDinh();
        int bhytHetHan = bhytService.thongKeBHYTTheoHanThe("30");
        ThongKeChungResponse response = ThongKeChungResponse.builder()
                .tongSoKhachHang(tongSoKhachHang)
                .tongSoKhachHangChuaCoHo(tongSoKhachHangChuaCoHo)
                .tongSoKhachHangChuaCoHo(soHoGiaDinh)
                .bhytHetHan(bhytHetHan)
                .build();
        return MyApiResponse.<ThongKeChungResponse>builder()
                .data(response)
                .build();
    }

    @GetMapping("/tong-so-tien")
    public MyApiResponse<Integer> getTongSoTien(@RequestParam String tuNgay, @RequestParam String denNgay) {
        int tongSoTien = bhytService.thongKeTongTienThuDuoc(tuNgay, denNgay);
        return MyApiResponse.<Integer>builder()
                .data(tongSoTien)
                .code(200)
                .message("Thống kê tổng số tiền thu được từ ngày " + tuNgay + " đến ngày " + denNgay + ": " + tongSoTien)
                .build();
    }

    @GetMapping("/bhyt-moi")
    public MyApiResponse<Integer> getBHYTMoi(@RequestParam String tuNgay, @RequestParam String denNgay) {
        int bhytMoi = bhytService.thongKeBHYTMoi(tuNgay, denNgay);
        return MyApiResponse.<Integer>builder()
                .data(bhytMoi)
                .code(200)
                .message("Thống kê số lượng BHYT mới từ ngày " + tuNgay + " đến ngày " + denNgay + ": " + bhytMoi)
                .build();
    }

    @GetMapping("/bhyt-gia-han")
    public MyApiResponse<Integer> getBHYTGiaHan(@RequestParam String tuNgay, @RequestParam String denNgay) {
        int bhytGiaHan = bhytService.thongKeBHYTGiaHan(tuNgay, denNgay);
        return MyApiResponse.<Integer>builder()
                .data(bhytGiaHan)
                .code(200)
                .message("Thống kê số lượng BHYT gia hạn từ ngày " + tuNgay + " đến ngày " + denNgay + ": " + bhytGiaHan)
                .build();
    }
}

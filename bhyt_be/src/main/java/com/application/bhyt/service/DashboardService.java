package com.application.bhyt.service;

import com.application.bhyt.dto.response.ThongKeDoanhThuDto;
import com.application.bhyt.dto.response.ThongKeTongQuanDto;
import com.application.bhyt.enums.ErrorCode;
import com.application.bhyt.enums.LoaiBHYT;
import com.application.bhyt.exception.MyException;
import com.application.bhyt.repository.BaoHiemYTeRepository;
import com.application.bhyt.repository.HoGiaDinhRepository;
import com.application.bhyt.repository.KhachHangRepository;
import com.application.bhyt.util.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Nghiệp vụ Trang 1 (Thống kê). Chủ yếu là các câu đếm/tổng trên repository.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final BaoHiemYTeRepository baoHiemYTeRepository;
    private final KhachHangRepository khachHangRepository;
    private final HoGiaDinhRepository hoGiaDinhRepository;

    /** 5 chỉ số đếm của dashboard. */
    public ThongKeTongQuanDto tongQuan() {
        LocalDate homNay = LocalDate.now();
        return ThongKeTongQuanDto.builder()
                .bhytSapHetHan30Ngay(baoHiemYTeRepository.demSapHetHan(homNay, homNay.plusDays(30)))
                .tongHoGiaDinh(hoGiaDinhRepository.count())
                .bhytChuaNhanHoaHong(baoHiemYTeRepository.demChuaNhanHoaHongHienHanh())
                .tongKhachHang(khachHangRepository.demConHieuLuc())
                .khachHangChuaCoHo(khachHangRepository.demKhachHangChuaCoHo())
                .build();
    }

    /** Doanh thu + số thẻ mới/gia hạn theo khoảng ngày (lọc trên ngay_mua). */
    public ThongKeDoanhThuDto doanhThu(String tuNgayStr, String denNgayStr) {
        LocalDate tuNgay = DateUtils.parse(tuNgayStr);
        LocalDate denNgay = DateUtils.parse(denNgayStr);
        if (tuNgay == null || denNgay == null) {
            throw new MyException(ErrorCode.KHOANG_NGAY_KHONG_HOP_LE, "Thiếu tuNgay hoặc denNgay");
        }
        if (tuNgay.isAfter(denNgay)) {
            throw new MyException(ErrorCode.KHOANG_NGAY_KHONG_HOP_LE, "tuNgay phải <= denNgay");
        }

        long soTheMoi = baoHiemYTeRepository.demTheTheoLoaiVaNgayMua(LoaiBHYT.MOI.getGiaTri(), tuNgay, denNgay);
        long soTheGiaHan = baoHiemYTeRepository.demTheTheoLoaiVaNgayMua(LoaiBHYT.GIA_HAN.getGiaTri(), tuNgay, denNgay);

        return ThongKeDoanhThuDto.builder()
                .tuNgay(DateUtils.format(tuNgay))
                .denNgay(DateUtils.format(denNgay))
                .tongTien(baoHiemYTeRepository.tongTienTheoNgayMua(tuNgay, denNgay))
                .tongThe(baoHiemYTeRepository.demTheTheoNgayMua(tuNgay, denNgay))
                .soTheMoi(soTheMoi)
                .soTheGiaHan(soTheGiaHan)
                .build();
    }
}

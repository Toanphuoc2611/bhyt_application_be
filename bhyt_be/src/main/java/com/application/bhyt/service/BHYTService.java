package com.application.bhyt.service;

import com.application.bhyt.enums.LoaiBHYT;
import com.application.bhyt.repository.BHYTRepository;
import com.application.bhyt.repository.HoGiaDinhRepository;
import com.application.bhyt.repository.KhachHangRepository;
import com.application.bhyt.util.DateUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class BHYTService {
    BHYTRepository repository;

    /**
     * Thống kê số lượng thẻ BHYT theo hạn thẻ (tính từ ngày hiện tại)
     * @param soNgay: số ngày từ ngày hiện tại đến hạn thẻ
     * @return số lượng thẻ BHYT theo hạn thẻ
     */
    public Integer thongKeBHYTTheoHanThe(String soNgay) {
        LocalDate hienTai = LocalDate.now();
        try {
            LocalDate toDate = hienTai.plus(Integer.parseInt(soNgay), java.time.temporal.ChronoUnit.DAYS);
            return repository.thongKeBHYTTheoHanThe(hienTai, toDate);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("soNgay không hợp lệ: " + soNgay);
        }
    }


    /**
     * Thống kê tổng số BHYT chưa nhận hoa hồng
     * @return
     */
    public Integer thongKeTongSoBHYTChuaNhanHoaHong() {
        Integer result = repository.countChuaNhanHoaHong();
        return result == null ? 0 : result;
    }

    /**
     * Thống kê tổng tiền thu được trong khoảng thời gian từ ngày from đến ngày to
     * @param tuNgay
     * @param denNgay
     * @return
     */
    public Integer thongKeTongTienThuDuoc(String tuNgay, String denNgay) {
        LocalDate fromDate = DateUtils.toLocalDate(tuNgay);
        LocalDate toDate = DateUtils.toLocalDate(denNgay);
        Integer result = repository.sumSoTienThuByNgayTaoBetween(fromDate, toDate);
        return result == null ? 0 : result;
    }

    /**
     * Thống kê số lượng BHYT mới trong khoảng thời gian từ ngày from đến ngày to
     * @param tuNgay
     * @param denNgay
     * @return
     */
    public Integer thongKeBHYTMoi(String tuNgay, String denNgay) {
        LocalDate fromDate = DateUtils.toLocalDate(tuNgay);
        LocalDate toDate = DateUtils.toLocalDate(denNgay);
        Integer result = repository.countByLoaiAndNgayTaoBetween(LoaiBHYT.MOI.getLoai(), fromDate, toDate);
        return result == null ? 0 : result;
    }

    /**
     * Thống kê số lượng BHYT gia hạn trong khoảng thời gian từ ngày from đến ngày to
     * @param tuNgay
     * @param denNgay
     * @return
     */
    public Integer thongKeBHYTGiaHan(String tuNgay, String denNgay) {
        LocalDate fromDate = DateUtils.toLocalDate(tuNgay);
        LocalDate toDate = DateUtils.toLocalDate(denNgay);
        Integer result = repository.countByLoaiAndNgayTaoBetween(LoaiBHYT.GIA_HAN.getLoai(), fromDate, toDate);
        return result == null ? 0 : result;
    }
}

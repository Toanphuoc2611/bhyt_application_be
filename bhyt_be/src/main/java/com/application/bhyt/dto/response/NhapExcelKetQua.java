package com.application.bhyt.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Báo cáo kết quả nhập dữ liệu từ file Excel.
 */
@Data
public class NhapExcelKetQua {

    private int soHoGiaDinh;
    private int soKhachHang;
    private int soThe;

    /** Các dòng bị bỏ qua kèm lý do (ví dụ thiếu họ tên). */
    private final List<String> boQua = new ArrayList<>();

    /** Cảnh báo lệch số liệu so với file (tiền / hạn thẻ tính lại khác giá trị trong sheet). */
    private final List<String> canhBao = new ArrayList<>();

    public String tomTat() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== KẾT QUẢ NHẬP EXCEL ===\n");
        sb.append("Hộ gia đình : ").append(soHoGiaDinh).append('\n');
        sb.append("Khách hàng  : ").append(soKhachHang).append('\n');
        sb.append("Thẻ BHYT    : ").append(soThe).append('\n');
        sb.append("Bỏ qua      : ").append(boQua.size()).append('\n');
        boQua.forEach(s -> sb.append("  - ").append(s).append('\n'));
        sb.append("Cảnh báo lệch số liệu: ").append(canhBao.size()).append('\n');
        canhBao.forEach(s -> sb.append("  ! ").append(s).append('\n'));
        return sb.toString();
    }
}

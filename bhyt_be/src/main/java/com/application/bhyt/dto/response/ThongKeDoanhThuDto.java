package com.application.bhyt.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * Chỉ số doanh thu Trang 1 theo khoảng ngày (lọc trên {@code ngay_mua}).
 * Đủ dữ liệu để FE vẽ biểu đồ tách "mới" / "gia hạn" và hiển thị số liệu thô.
 */
@Data
@Builder
public class ThongKeDoanhThuDto {

    private String tuNgay;   // dd/MM/yyyy
    private String denNgay;  // dd/MM/yyyy

    /** Tổng tiền thu được trong khoảng (VNĐ). */
    private long tongTien;

    /** Tổng số thẻ bán trong khoảng. */
    private long tongThe;

    /** Số thẻ loại "mới". */
    private long soTheMoi;

    /** Số thẻ loại "gia hạn". */
    private long soTheGiaHan;
}

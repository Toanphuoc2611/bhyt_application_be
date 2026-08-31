package com.application.bhyt.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * 5 chỉ số đếm của Trang 1 (Thống kê) trả về trong một response.
 */
@Data
@Builder
public class ThongKeTongQuanDto {

    /** Số thẻ sắp hết hạn trong vòng 30 ngày (hôm nay <= han_the <= hôm nay + 30). */
    private long bhytSapHetHan30Ngay;

    /** Tổng số hộ gia đình. */
    private long tongHoGiaDinh;

    /** Số thẻ hiện hành chưa nhận hoa hồng (da_nhan_hoa_hong = 0, bhyt_moi_nhat = 1). */
    private long bhytChuaNhanHoaHong;

    /** Tổng số khách hàng chưa bị xóa. */
    private long tongKhachHang;

    /** Số khách hàng chưa thuộc hộ nào. */
    private long khachHangChuaCoHo;
}

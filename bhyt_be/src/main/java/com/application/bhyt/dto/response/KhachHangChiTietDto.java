package com.application.bhyt.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Dữ liệu cho màn hình chi tiết khách hàng (dùng chung Trang 2 và Trang 3).
 * Là kết quả tổng hợp từ 3 bảng nên không thể trả entity trực tiếp.
 */
@Data
@Builder
public class KhachHangChiTietDto {

    /** Thông tin cơ bản + thẻ hiện hành + hộ hiện tại. */
    private KhachHangDto khachHang;

    /** Toàn bộ lịch sử mua BHYT của khách hàng (mới nhất trước). */
    private List<BaoHiemYTeDto> lichSuBhyt;

    /**
     * Các thành viên khác trong cùng hộ (kèm thẻ hiện hành của họ).
     * Rỗng nếu khách hàng không thuộc hộ nào.
     */
    private List<ThanhVienHoGiaDinhDto> thanhVienCungHo;
}

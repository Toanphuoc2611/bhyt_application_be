package com.application.bhyt.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * Thông tin khách hàng dùng cho danh sách Trang 3 và màn hình xem/sửa đơn lẻ.
 * Kèm sẵn hộ gia đình hiện tại và thẻ BHYT hiện hành để dựng các cột "BHYT", "HỘ".
 */
@Data
@Builder
public class KhachHangDto {

    private Integer id;
    private String cccd;
    private String hoVaTen;
    private String ngaySinh;    // dd/MM/yyyy
    private String diaChi;
    private String soDienThoai;
    private String lienLacKhac;
    private String hinhAnh;
    private String ghiChu;
    private String bhytKhac;
    private Integer coMstb;
    private Integer daXoa;
    private String ngayTao;
    private String ngayCapNhat;

    /** Hộ gia đình hiện tại (null nếu khách hàng chưa thuộc hộ nào). */
    private Integer idHoGiaDinh;

    /** Thẻ BHYT hiện hành (null nếu chưa mua BHYT). */
    private BaoHiemYTeDto bhytHienTai;
}

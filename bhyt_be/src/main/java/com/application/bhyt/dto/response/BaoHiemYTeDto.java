package com.application.bhyt.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * Thông tin một thẻ BHYT trả về cho FE.
 * Đã kèm sẵn các trường của khách hàng để dựng bảng Trang 2 mà không cần gọi thêm API.
 */
@Data
@Builder
public class BaoHiemYTeDto {

    private Integer id;

    // ----- Thông tin khách hàng (join sẵn) -----
    private Integer idKhachHang;
    private String cccd;
    private String hoVaTen;
    private String ngaySinh;   // dd/MM/yyyy
    private String diaChi;
    /** Số điện thoại - dùng cho cột "LIÊN HỆ". */
    private String soDienThoai;
    private String lienLacKhac;

    // ----- Thông tin thẻ -----
    private String ngayMua;        // dd/MM/yyyy
    private String ngayHetHanCu;   // dd/MM/yyyy - hạn thẻ cũ (khi gia hạn)
    private Integer soThangMua;
    private Boolean goiMacDinh;    // true = ô "Tháng mua" để trống
    private String hanThe;         // dd/MM/yyyy
    private Integer soLanMuaCuaHo;
    private Integer soTienThu;
    private Integer daNhanHoaHong;
    private Integer bhytMoiNhat;
    private Integer daXoa;
    private String loai;        // "mới" | "gia hạn"
    private String noiDangKy;
    private String ngayTao;
    private String ngayCapNhat;
}

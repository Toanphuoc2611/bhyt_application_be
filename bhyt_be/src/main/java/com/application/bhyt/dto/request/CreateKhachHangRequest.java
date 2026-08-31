package com.application.bhyt.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Dữ liệu tạo khách hàng mới (Trang 3). */
@Data
public class CreateKhachHangRequest {

    @NotBlank(message = "CCCD là bắt buộc")
    private String cccd;

    @NotBlank(message = "Họ và tên là bắt buộc")
    private String hoVaTen;

    /** dd/MM/yyyy */
    private String ngaySinh;
    private String diaChi;
    private String soDienThoai;
    private String lienLacKhac;
    private String hinhAnh;
    private String ghiChu;
    private String bhytKhac;

    /** 0 = không có mstb, 1 = có. Mặc định 0. */
    private Integer coMstb = 0;

    /** Tùy chọn: gán luôn khách hàng vào hộ này khi tạo. */
    private Integer idHoGiaDinh;
}

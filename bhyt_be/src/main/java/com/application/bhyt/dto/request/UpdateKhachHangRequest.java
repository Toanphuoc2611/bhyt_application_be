package com.application.bhyt.dto.request;

import lombok.Data;

/** Dữ liệu cập nhật khách hàng. Trường nào null thì giữ nguyên giá trị cũ. */
@Data
public class UpdateKhachHangRequest {

    private String hoVaTen;
    private String ngaySinh;   // dd/MM/yyyy
    private String diaChi;
    private String soDienThoai;
    private String lienLacKhac;
    private String hinhAnh;
    private String ghiChu;
    private String bhytKhac;
    private Integer coMstb;
}

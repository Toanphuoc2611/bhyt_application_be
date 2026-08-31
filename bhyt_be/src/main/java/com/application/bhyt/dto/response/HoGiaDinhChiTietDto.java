package com.application.bhyt.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/** Chi tiết hộ gia đình kèm danh sách thành viên hiện tại. */
@Data
@Builder
public class HoGiaDinhChiTietDto {

    private Integer id;
    private String ten;
    private Integer soThanhVien;
    private String hinhAnh;
    private String ngayTao;
    private String ngayCapNhat;

    /** Các thành viên đang hoạt động của hộ (kèm thẻ BHYT hiện hành). */
    private List<ThanhVienHoGiaDinhDto> thanhVienHienTai;
}

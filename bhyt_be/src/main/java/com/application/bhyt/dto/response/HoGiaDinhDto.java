package com.application.bhyt.dto.response;

import lombok.Builder;
import lombok.Data;

/** Thông tin tóm tắt một hộ gia đình (dùng cho danh sách). */
@Data
@Builder
public class HoGiaDinhDto {

    private Integer id;

    /** Tên/nhãn hộ (tên chủ hộ hoặc biệt danh); có thể null. */
    private String ten;

    /** Số thành viên đang hoạt động (ngay_ket_thuc IS NULL). */
    private Integer soThanhVien;

    private String hinhAnh;
    private String ngayTao;
    private String ngayCapNhat;
}

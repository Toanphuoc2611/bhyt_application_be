package com.application.bhyt.dto.request;

import lombok.Data;

/** Dữ liệu tạo mới / cập nhật hộ gia đình. */
@Data
public class LuuHoGiaDinhRequest {

    /** Tên/nhãn hộ (tên chủ hộ hoặc biệt danh). */
    private String ten;

    /** Đường dẫn ảnh sổ hộ khẩu / VNeID. */
    private String hinhAnh;
}

package com.application.bhyt.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Thêm một khách hàng vào hộ gia đình. */
@Data
public class ThemThanhVienRequest {

    @NotNull(message = "Thiếu ID khách hàng")
    private Integer idKhachHang;
}

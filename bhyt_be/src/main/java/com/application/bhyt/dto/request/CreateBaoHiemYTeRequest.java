package com.application.bhyt.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * Dữ liệu tạo mới thẻ BHYT ({@code loai = "mới"}).
 * Server tự tính {@code hanThe} và {@code soTienThu}.
 */
@Data
public class CreateBaoHiemYTeRequest {

    @NotNull(message = "Thiếu ID khách hàng")
    private Integer idKhachHang;

    @NotBlank(message = "Ngày mua là bắt buộc")
    private String ngayMua;      // dd/MM/yyyy

    /**
     * Số tháng mua. Để trống = "gói mặc định" (dùng số tháng cấu hình, và hạn thẻ
     * mới được cộng thêm 1 tháng theo công thức Excel).
     */
    @Positive(message = "Số tháng mua phải lớn hơn 0")
    private Integer soThangMua;

    /**
     * Bậc "Người thứ" trong hộ - do người dùng nhập ở FE, BẮT BUỘC, không null.
     * 1 -> 100%, 2 -> 70%, 3 -> 60%, 4 -> 50%, từ 5 -> 40% (theo file cấu hình).
     */
    @NotNull(message = "Số lần mua của hộ là bắt buộc")
    @Positive(message = "Số lần mua của hộ phải lớn hơn 0")
    private Integer soLanMuaCuaHo;

    private String noiDangKy;
}

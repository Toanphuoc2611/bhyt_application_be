package com.application.bhyt.dto.request;

import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * Dữ liệu cập nhật thẻ BHYT. Trường nào null thì giữ nguyên.
 * Không cho sửa {@code loai} và khách hàng của thẻ.
 * Sau khi cập nhật, server tính lại {@code hanThe} và {@code soTienThu}.
 */
@Data
public class UpdateBaoHiemYTeRequest {

    private String ngayMua;         // dd/MM/yyyy

    /** Hạn thẻ cũ (chỉ có ý nghĩa với thẻ gia hạn). */
    private String ngayHetHanCu;    // dd/MM/yyyy

    @Positive(message = "Số tháng mua phải lớn hơn 0")
    private Integer soThangMua;

    /** Đặt true để chuyển thẻ về "gói mặc định" (bỏ số tháng tùy chọn). */
    private Boolean goiMacDinh;

    @Positive(message = "Số lần mua của hộ phải lớn hơn 0")
    private Integer soLanMuaCuaHo;

    private String noiDangKy;
}

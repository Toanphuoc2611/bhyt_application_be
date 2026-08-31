package com.application.bhyt.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * Dữ liệu gia hạn một thẻ BHYT.
 * Tạo dòng mới {@code loai = "gia hạn"}, GIỮ NGUYÊN bậc {@code soLanMuaCuaHo}
 * của thẻ được gia hạn. Hạn thẻ mới tính từ hạn thẻ cũ.
 */
@Data
public class GiaHanBaoHiemYTeRequest {

    @NotBlank(message = "Ngày mua là bắt buộc")
    private String ngayMua;         // dd/MM/yyyy

    /**
     * Hạn thẻ cũ dùng làm mốc tính. Để trống = lấy {@code hanThe} của thẻ đang gia hạn.
     */
    private String ngayHetHanCu;    // dd/MM/yyyy

    /** Số tháng mua. Để trống = gói mặc định. */
    @Positive(message = "Số tháng mua phải lớn hơn 0")
    private Integer soThangMua;

    private String noiDangKy;
}

package com.application.bhyt.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * Một thành viên trong hộ gia đình, kèm thẻ BHYT hiện hành của người đó.
 * Dùng cho màn hình chi tiết (hiển thị "những người cùng hộ").
 */
@Data
@Builder
public class ThanhVienHoGiaDinhDto {

    /** ID dòng thanh_vien_ho_gia_dinh. */
    private Integer id;

    /** ID hộ gia đình. */
    private Integer idHoGiaDinh;

    /** ID khách hàng. */
    private Integer idThanhVien;

    private String hoVaTen;
    private String cccd;
    private String ngaySinh;
    private String diaChi;
    private String soDienThoai;
    private Integer coMstb;

    /** Ngày tham gia hộ. */
    private String ngayTao;

    /** Ngày rời hộ (null nếu vẫn đang là thành viên). */
    private String ngayKetThuc;

    /** Thẻ BHYT hiện hành của thành viên này (null nếu chưa có). */
    private BaoHiemYTeDto bhytHienTai;
}

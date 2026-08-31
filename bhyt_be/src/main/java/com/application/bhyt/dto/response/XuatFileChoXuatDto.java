package com.application.bhyt.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Dữ liệu cho Trang 4 (Xuất file Excel). Việc sinh file .xlsx do FE thực hiện;
 * backend chỉ cung cấp đủ 2 phần dữ liệu:
 * <ul>
 *   <li>{@code danhSach}: các dòng thẻ BHYT đúng thứ tự cột client yêu cầu;</li>
 *   <li>{@code danhSachHo}: bảng "ds hộ" - roster các hộ gia đình.</li>
 * </ul>
 */
@Data
@Builder
public class XuatFileChoXuatDto {

    private List<DongXuatFileDto> danhSach;
    private List<HoRosterDto> danhSachHo;

    /** Một dòng của sheet chính. Thứ tự cột: HỘ, HỌ VÀ TÊN, NĂM SINH, ĐỊA CHỈ,
     *  NGÀY MUA, CCCD, LOẠI, NƠI ĐK, GHI CHÚ, SỐ LẦN MUA CỦA HỘ, THÀNH TIỀN. */
    @Data
    @Builder
    public static class DongXuatFileDto {
        /** ID hộ gia đình của khách hàng (null nếu không thuộc hộ nào). */
        private Integer ho;
        private String hoVaTen;
        /** Chỉ lấy năm, trích từ khach_hang.ngay_sinh. */
        private Integer namSinh;
        private String diaChi;
        private String ngayMua;   // dd/MM/yyyy
        private String cccd;
        private String loai;
        private String noiDangKy;
        private String ghiChu;
        private Integer soLanMuaCuaHo;
        /** = so_tien_thu. */
        private Integer thanhTien;
    }

    /** Một dòng của sheet "ds hộ". */
    @Data
    @Builder
    public static class HoRosterDto {
        private Integer idHo;
        private Integer soThanhVien;
        /** Danh sách "Họ tên - CCCD" các thành viên hiện tại. */
        private List<String> thanhVien;
    }
}

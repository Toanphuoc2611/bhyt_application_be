package com.application.bhyt.mapper;

import com.application.bhyt.dto.response.BaoHiemYTeDto;
import com.application.bhyt.entity.BaoHiemYTe;
import com.application.bhyt.entity.KhachHang;
import com.application.bhyt.util.DateUtils;
import org.springframework.stereotype.Component;

/**
 * Chuyển entity {@link BaoHiemYTe} sang {@link BaoHiemYTeDto}.
 * Phải gọi trong phạm vi giao dịch vì có truy cập trường lazy {@code khachHang}.
 */
@Component
public class BaoHiemYTeMapper {

    public BaoHiemYTeDto toDto(BaoHiemYTe b) {
        if (b == null) {
            return null;
        }
        KhachHang kh = b.getKhachHang();
        return BaoHiemYTeDto.builder()
                .id(b.getId())
                .idKhachHang(kh != null ? kh.getId() : null)
                .cccd(kh != null ? kh.getCccd() : null)
                .hoVaTen(kh != null ? kh.getHoVaTen() : null)
                .ngaySinh(kh != null ? DateUtils.format(kh.getNgaySinh()) : null)
                .diaChi(kh != null ? kh.getDiaChi() : null)
                .soDienThoai(kh != null ? kh.getSoDienThoai() : null)
                .lienLacKhac(kh != null ? kh.getLienLacKhac() : null)
                .ngayMua(DateUtils.format(b.getNgayMua()))
                .ngayHetHanCu(DateUtils.format(b.getNgayHetHanCu()))
                .soThangMua(b.getSoThangMua())
                .goiMacDinh(b.getGoiMacDinh())
                .hanThe(DateUtils.format(b.getHanThe()))
                .soLanMuaCuaHo(b.getSoLanMuaCuaHo())
                .soTienThu(b.getSoTienThu())
                .daNhanHoaHong(b.getDaNhanHoaHong())
                .bhytMoiNhat(b.getBhytMoiNhat())
                .daXoa(b.getDaXoa())
                .loai(b.getLoai())
                .noiDangKy(b.getNoiDangKy())
                .ngayTao(DateUtils.format(b.getNgayTao()))
                .ngayCapNhat(DateUtils.format(b.getNgayCapNhat()))
                .build();
    }
}

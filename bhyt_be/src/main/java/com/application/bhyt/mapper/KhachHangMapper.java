package com.application.bhyt.mapper;

import com.application.bhyt.dto.response.KhachHangDto;
import com.application.bhyt.entity.BaoHiemYTe;
import com.application.bhyt.entity.KhachHang;
import com.application.bhyt.entity.ThanhVienHoGiaDinh;
import com.application.bhyt.util.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Chuyển entity {@link KhachHang} sang {@link KhachHangDto}. */
@Component
@RequiredArgsConstructor
public class KhachHangMapper {

    private final BaoHiemYTeMapper baoHiemYTeMapper;

    /**
     * @param kh           khách hàng
     * @param hoHienTai    dòng thành viên hộ đang hoạt động (null nếu không thuộc hộ nào)
     * @param theHienHanh  thẻ BHYT hiện hành (null nếu chưa mua)
     */
    public KhachHangDto toDto(KhachHang kh, ThanhVienHoGiaDinh hoHienTai, BaoHiemYTe theHienHanh) {
        return KhachHangDto.builder()
                .id(kh.getId())
                .cccd(kh.getCccd())
                .hoVaTen(kh.getHoVaTen())
                .ngaySinh(DateUtils.format(kh.getNgaySinh()))
                .diaChi(kh.getDiaChi())
                .soDienThoai(kh.getSoDienThoai())
                .lienLacKhac(kh.getLienLacKhac())
                .hinhAnh(kh.getHinhAnh())
                .ghiChu(kh.getGhiChu())
                .bhytKhac(kh.getBhytKhac())
                .coMstb(kh.getCoMstb())
                .daXoa(kh.getDaXoa())
                .ngayTao(DateUtils.format(kh.getNgayTao()))
                .ngayCapNhat(DateUtils.format(kh.getNgayCapNhat()))
                .idHoGiaDinh(hoHienTai != null ? hoHienTai.getHoGiaDinh().getId() : null)
                .bhytHienTai(baoHiemYTeMapper.toDto(theHienHanh))
                .build();
    }
}

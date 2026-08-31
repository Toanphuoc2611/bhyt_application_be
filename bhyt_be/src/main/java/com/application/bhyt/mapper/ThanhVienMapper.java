package com.application.bhyt.mapper;

import com.application.bhyt.dto.response.ThanhVienHoGiaDinhDto;
import com.application.bhyt.entity.BaoHiemYTe;
import com.application.bhyt.entity.KhachHang;
import com.application.bhyt.entity.ThanhVienHoGiaDinh;
import com.application.bhyt.util.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Chuyển entity {@link ThanhVienHoGiaDinh} sang {@link ThanhVienHoGiaDinhDto}. */
@Component
@RequiredArgsConstructor
public class ThanhVienMapper {

    private final BaoHiemYTeMapper baoHiemYTeMapper;

    /**
     * @param tv           dòng thành viên hộ
     * @param theHienHanh  thẻ BHYT hiện hành của thành viên (null nếu chưa có)
     */
    public ThanhVienHoGiaDinhDto toDto(ThanhVienHoGiaDinh tv, BaoHiemYTe theHienHanh) {
        KhachHang kh = tv.getKhachHang();
        return ThanhVienHoGiaDinhDto.builder()
                .id(tv.getId())
                .idHoGiaDinh(tv.getHoGiaDinh().getId())
                .idThanhVien(kh.getId())
                .hoVaTen(kh.getHoVaTen())
                .cccd(kh.getCccd())
                .ngaySinh(DateUtils.format(kh.getNgaySinh()))
                .diaChi(kh.getDiaChi())
                .soDienThoai(kh.getSoDienThoai())
                .coMstb(kh.getCoMstb())
                .ngayTao(DateUtils.format(tv.getNgayTao()))
                .ngayKetThuc(DateUtils.format(tv.getNgayKetThuc()))
                .bhytHienTai(baoHiemYTeMapper.toDto(theHienHanh))
                .build();
    }
}

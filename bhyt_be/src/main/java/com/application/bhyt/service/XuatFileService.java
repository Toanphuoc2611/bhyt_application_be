package com.application.bhyt.service;

import com.application.bhyt.dto.response.XuatFileChoXuatDto;
import com.application.bhyt.dto.response.XuatFileChoXuatDto.DongXuatFileDto;
import com.application.bhyt.dto.response.XuatFileChoXuatDto.HoRosterDto;
import com.application.bhyt.entity.BaoHiemYTe;
import com.application.bhyt.entity.KhachHang;
import com.application.bhyt.entity.ThanhVienHoGiaDinh;
import com.application.bhyt.repository.BaoHiemYTeRepository;
import com.application.bhyt.repository.HoGiaDinhRepository;
import com.application.bhyt.repository.ThanhVienHoGiaDinhRepository;
import com.application.bhyt.util.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Cung cấp DỮ LIỆU cho Trang 4 (Xuất file Excel).
 *
 * <p>Việc sinh file .xlsx chọn lọc do FE thực hiện; backend chỉ trả về danh sách
 * thẻ theo đúng thứ tự cột client yêu cầu và bảng "ds hộ". Không có khái niệm
 * "đã xuất" / "hoàn tác" ở backend.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class XuatFileService {

    private final BaoHiemYTeRepository baoHiemYTeRepository;
    private final HoGiaDinhRepository hoGiaDinhRepository;
    private final ThanhVienHoGiaDinhRepository thanhVienHoGiaDinhRepository;

    public XuatFileChoXuatDto layDuLieuChoXuat() {
        List<DongXuatFileDto> danhSach = baoHiemYTeRepository.findChoXuatFile().stream()
                .map(this::toDongXuat)
                .toList();

        List<HoRosterDto> danhSachHo = hoGiaDinhRepository.findAll().stream()
                .map(ho -> {
                    List<String> tv = thanhVienHoGiaDinhRepository.findThanhVienDangHoatDong(ho.getId()).stream()
                            .map(m -> {
                                KhachHang k = m.getKhachHang();
                                return k.getHoVaTen() + " - " + (k.getCccd() != null ? k.getCccd() : "");
                            })
                            .toList();
                    return HoRosterDto.builder()
                            .idHo(ho.getId())
                            .soThanhVien(tv.size())
                            .thanhVien(tv)
                            .build();
                })
                .toList();

        return XuatFileChoXuatDto.builder()
                .danhSach(danhSach)
                .danhSachHo(danhSachHo)
                .build();
    }

    private DongXuatFileDto toDongXuat(BaoHiemYTe b) {
        KhachHang kh = b.getKhachHang();
        ThanhVienHoGiaDinh ho = thanhVienHoGiaDinhRepository.findHoHienTaiCuaKhachHang(kh.getId()).orElse(null);
        return DongXuatFileDto.builder()
                .ho(ho != null ? ho.getHoGiaDinh().getId() : null)
                .hoVaTen(kh.getHoVaTen())
                .namSinh(kh.getNgaySinh() != null ? kh.getNgaySinh().getYear() : null)
                .diaChi(kh.getDiaChi())
                .ngayMua(DateUtils.format(b.getNgayMua()))
                .cccd(kh.getCccd())
                .loai(b.getLoai())
                .noiDangKy(b.getNoiDangKy())
                .ghiChu(kh.getGhiChu())
                .soLanMuaCuaHo(b.getSoLanMuaCuaHo())
                .thanhTien(b.getSoTienThu())
                .build();
    }
}

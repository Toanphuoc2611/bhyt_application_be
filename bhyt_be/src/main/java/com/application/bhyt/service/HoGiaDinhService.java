package com.application.bhyt.service;

import com.application.bhyt.dto.request.LuuHoGiaDinhRequest;
import com.application.bhyt.dto.request.ThemThanhVienRequest;
import com.application.bhyt.dto.response.HoGiaDinhChiTietDto;
import com.application.bhyt.dto.response.HoGiaDinhDto;
import com.application.bhyt.dto.response.PageResponse;
import com.application.bhyt.dto.response.ThanhVienHoGiaDinhDto;
import com.application.bhyt.entity.HoGiaDinh;
import com.application.bhyt.entity.KhachHang;
import com.application.bhyt.entity.ThanhVienHoGiaDinh;
import com.application.bhyt.enums.ActionType;
import com.application.bhyt.enums.ErrorCode;
import com.application.bhyt.exception.MyException;
import com.application.bhyt.mapper.ThanhVienMapper;
import com.application.bhyt.repository.BaoHiemYTeRepository;
import com.application.bhyt.repository.HoGiaDinhRepository;
import com.application.bhyt.repository.KhachHangRepository;
import com.application.bhyt.repository.ThanhVienHoGiaDinhRepository;
import com.application.bhyt.util.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Nghiệp vụ quản lý hộ gia đình (Trang 3).
 *
 * <p>Quy ước: một người "rời hộ" bằng cách set {@code ngay_ket_thuc}, không xóa
 * dòng. Mỗi khách hàng tối đa thuộc 1 hộ tại một thời điểm.
 * {@code so_thanh_vien} luôn được tính lại từ số thành viên đang hoạt động.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class HoGiaDinhService {

    private final HoGiaDinhRepository hoGiaDinhRepository;
    private final KhachHangRepository khachHangRepository;
    private final ThanhVienHoGiaDinhRepository thanhVienHoGiaDinhRepository;
    private final BaoHiemYTeRepository baoHiemYTeRepository;
    private final ThanhVienMapper thanhVienMapper;
    private final AuditLogService auditLogService;

    public HoGiaDinhDto taoMoi(LuuHoGiaDinhRequest req) {
        HoGiaDinh ho = new HoGiaDinh();
        ho.setSoThanhVien(0);
        if (req != null) {
            ho.setHinhAnh(req.getHinhAnh());
            ho.setTen(req.getTen());
        }
        ho = hoGiaDinhRepository.save(ho);
        auditLogService.ghiLai(ActionType.TAO_HO_GIA_DINH, "ho_gia_dinh", ho.getId(), "Tạo hộ gia đình mới");
        log.info("Tạo hộ gia đình thành công - id={}", ho.getId());
        return toDto(ho);
    }

    public HoGiaDinhDto capNhat(Integer id, LuuHoGiaDinhRequest req) {
        HoGiaDinh ho = layHo(id);
        if (req.getHinhAnh() != null) ho.setHinhAnh(req.getHinhAnh());
        if (req.getTen() != null) ho.setTen(req.getTen());
        ho = hoGiaDinhRepository.save(ho);
        auditLogService.ghiLai(ActionType.CAP_NHAT_HO_GIA_DINH, "ho_gia_dinh", id, "Cập nhật hộ gia đình");
        return toDto(ho);
    }

    @Transactional(readOnly = true)
    public PageResponse<HoGiaDinhDto> danhSach(Pageable pageable) {
        return PageResponse.from(hoGiaDinhRepository.findAll(pageable), this::toDto);
    }

    @Transactional(readOnly = true)
    public HoGiaDinhChiTietDto chiTiet(Integer id) {
        HoGiaDinh ho = layHo(id);
        List<ThanhVienHoGiaDinh> thanhVien = thanhVienHoGiaDinhRepository.findThanhVienDangHoatDong(id);
        List<ThanhVienHoGiaDinhDto> dtos = thanhVien.stream()
                .map(tv -> thanhVienMapper.toDto(tv, theHienHanh(tv.getKhachHang().getId())))
                .toList();
        return HoGiaDinhChiTietDto.builder()
                .id(ho.getId())
                .ten(ho.getTen())
                .soThanhVien(thanhVien.size())
                .hinhAnh(ho.getHinhAnh())
                .ngayTao(DateUtils.format(ho.getNgayTao()))
                .ngayCapNhat(DateUtils.format(ho.getNgayCapNhat()))
                .thanhVienHienTai(dtos)
                .build();
    }

    /** Thêm một khách hàng vào hộ. */
    public void themThanhVien(Integer idHo, ThemThanhVienRequest req) {
        HoGiaDinh ho = layHo(idHo);
        KhachHang kh = khachHangRepository.findActiveById(req.getIdKhachHang())
                .orElseThrow(() -> new MyException(ErrorCode.KHACH_HANG_KHONG_TON_TAI, "ID: " + req.getIdKhachHang()));

        if (thanhVienHoGiaDinhRepository.findHoHienTaiCuaKhachHang(kh.getId()).isPresent()) {
            throw new MyException(ErrorCode.KHACH_HANG_DA_CO_HO);
        }

        ThanhVienHoGiaDinh tv = new ThanhVienHoGiaDinh();
        tv.setKhachHang(kh);
        tv.setHoGiaDinh(ho);
        tv.setNgayTao(LocalDate.now());
        thanhVienHoGiaDinhRepository.save(tv);
        capNhatSoThanhVien(ho);

        auditLogService.ghiLai(ActionType.THEM_THANH_VIEN_HO, "thanh_vien_ho_gia_dinh", tv.getId(),
                "Thêm " + kh.getHoVaTen() + " vào hộ " + idHo);
        log.info("Thêm thành viên thành công - khachHang={}, ho={}", kh.getHoVaTen(), idHo);
    }

    /** Đưa một thành viên ra khỏi hộ: set ngay_ket_thuc, không xóa dòng. */
    public void xoaThanhVien(Integer idHo, Integer idKhachHang) {
        HoGiaDinh ho = layHo(idHo);
        ThanhVienHoGiaDinh tv = thanhVienHoGiaDinhRepository.findHoHienTaiCuaKhachHang(idKhachHang)
                .filter(m -> m.getHoGiaDinh().getId().equals(idHo))
                .orElseThrow(() -> new MyException(ErrorCode.THANH_VIEN_KHONG_TON_TAI,
                        "Khách hàng " + idKhachHang + " không thuộc hộ " + idHo));

        tv.setNgayKetThuc(LocalDate.now());
        thanhVienHoGiaDinhRepository.save(tv);
        capNhatSoThanhVien(ho);

        auditLogService.ghiLai(ActionType.XOA_THANH_VIEN_HO, "thanh_vien_ho_gia_dinh", tv.getId(),
                "Đưa khách hàng " + idKhachHang + " ra khỏi hộ " + idHo);
        log.info("Xóa thành viên khỏi hộ thành công - khachHang={}, ho={}", idKhachHang, idHo);
    }

    // ------------------------------------------------------------------

    private HoGiaDinh layHo(Integer id) {
        return hoGiaDinhRepository.findById(id)
                .orElseThrow(() -> new MyException(ErrorCode.HO_GIA_DINH_KHONG_TON_TAI, "ID: " + id));
    }

    private void capNhatSoThanhVien(HoGiaDinh ho) {
        ho.setSoThanhVien(thanhVienHoGiaDinhRepository.demThanhVienDangHoatDong(ho.getId()));
        hoGiaDinhRepository.save(ho);
    }

    private com.application.bhyt.entity.BaoHiemYTe theHienHanh(Integer idKhachHang) {
        return baoHiemYTeRepository.findTheHienHanh(idKhachHang).orElse(null);
    }

    private HoGiaDinhDto toDto(HoGiaDinh ho) {
        return HoGiaDinhDto.builder()
                .id(ho.getId())
                .ten(ho.getTen())
                .soThanhVien(thanhVienHoGiaDinhRepository.demThanhVienDangHoatDong(ho.getId()))
                .hinhAnh(ho.getHinhAnh())
                .ngayTao(DateUtils.format(ho.getNgayTao()))
                .ngayCapNhat(DateUtils.format(ho.getNgayCapNhat()))
                .build();
    }
}

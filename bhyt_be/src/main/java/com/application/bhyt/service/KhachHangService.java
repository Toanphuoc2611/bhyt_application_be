package com.application.bhyt.service;

import com.application.bhyt.dto.request.CreateKhachHangRequest;
import com.application.bhyt.dto.request.UpdateKhachHangRequest;
import com.application.bhyt.dto.response.BaoHiemYTeDto;
import com.application.bhyt.dto.response.KhachHangChiTietDto;
import com.application.bhyt.dto.response.KhachHangDto;
import com.application.bhyt.dto.response.PageResponse;
import com.application.bhyt.dto.response.ThanhVienHoGiaDinhDto;
import com.application.bhyt.entity.BaoHiemYTe;
import com.application.bhyt.entity.HoGiaDinh;
import com.application.bhyt.entity.KhachHang;
import com.application.bhyt.entity.ThanhVienHoGiaDinh;
import com.application.bhyt.enums.ActionType;
import com.application.bhyt.enums.BoLocKhachHang;
import com.application.bhyt.enums.ErrorCode;
import com.application.bhyt.exception.MyException;
import com.application.bhyt.mapper.BaoHiemYTeMapper;
import com.application.bhyt.mapper.KhachHangMapper;
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
 * Nghiệp vụ quản lý khách hàng (Trang 3) và màn hình chi tiết khách hàng
 * (dùng chung Trang 2 / Trang 3).
 *
 * <p>Xóa mềm ở mọi nơi: không bao giờ xóa vật lý bản ghi khách hàng.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class KhachHangService {

    private final KhachHangRepository khachHangRepository;
    private final HoGiaDinhRepository hoGiaDinhRepository;
    private final ThanhVienHoGiaDinhRepository thanhVienHoGiaDinhRepository;
    private final BaoHiemYTeRepository baoHiemYTeRepository;
    private final KhachHangMapper khachHangMapper;
    private final ThanhVienMapper thanhVienMapper;
    private final BaoHiemYTeMapper baoHiemYTeMapper;
    private final AuditLogService auditLogService;

    /** Tạo khách hàng mới; tùy chọn gán luôn vào một hộ. */
    public KhachHangDto taoMoi(CreateKhachHangRequest req) {
        if (khachHangRepository.findByCccd(req.getCccd().trim()).isPresent()) {
            throw new MyException(ErrorCode.CCCD_DA_TON_TAI, req.getCccd());
        }

        KhachHang kh = new KhachHang();
        kh.setCccd(req.getCccd().trim());
        kh.setHoVaTen(req.getHoVaTen().trim());
        kh.setNgaySinh(DateUtils.parse(req.getNgaySinh()));
        kh.setDiaChi(req.getDiaChi());
        kh.setSoDienThoai(req.getSoDienThoai());
        kh.setLienLacKhac(req.getLienLacKhac());
        kh.setHinhAnh(req.getHinhAnh());
        kh.setGhiChu(req.getGhiChu());
        kh.setBhytKhac(req.getBhytKhac());
        kh.setCoMstb(req.getCoMstb() != null ? req.getCoMstb() : 0);
        kh.setDaXoa(0);
        kh = khachHangRepository.save(kh);

        if (req.getIdHoGiaDinh() != null) {
            themVaoHo(kh, req.getIdHoGiaDinh());
        }

        auditLogService.ghiLai(ActionType.TAO_KHACH_HANG, "khach_hang", kh.getId(),
                "Tạo khách hàng " + kh.getHoVaTen());
        log.info("Tạo khách hàng thành công - id={}, cccd={}", kh.getId(), kh.getCccd());
        return toDto(kh);
    }

    /** Cập nhật khách hàng; trường null trong request nghĩa là giữ nguyên. */
    public KhachHangDto capNhat(Integer id, UpdateKhachHangRequest req) {
        KhachHang kh = layKhachHangConHieuLuc(id);

        if (req.getHoVaTen() != null) kh.setHoVaTen(req.getHoVaTen().trim());
        if (req.getNgaySinh() != null) kh.setNgaySinh(DateUtils.parse(req.getNgaySinh()));
        if (req.getDiaChi() != null) kh.setDiaChi(req.getDiaChi());
        if (req.getSoDienThoai() != null) kh.setSoDienThoai(req.getSoDienThoai());
        if (req.getLienLacKhac() != null) kh.setLienLacKhac(req.getLienLacKhac());
        if (req.getHinhAnh() != null) kh.setHinhAnh(req.getHinhAnh());
        if (req.getGhiChu() != null) kh.setGhiChu(req.getGhiChu());
        if (req.getBhytKhac() != null) kh.setBhytKhac(req.getBhytKhac());
        if (req.getCoMstb() != null) kh.setCoMstb(req.getCoMstb());

        kh = khachHangRepository.save(kh);
        auditLogService.ghiLai(ActionType.CAP_NHAT_KHACH_HANG, "khach_hang", kh.getId(),
                "Cập nhật khách hàng " + kh.getHoVaTen());
        log.info("Cập nhật khách hàng thành công - id={}", id);
        return toDto(kh);
    }

    /** Xóa mềm khách hàng (da_xoa = 1). */
    public void xoaMem(Integer id) {
        KhachHang kh = layKhachHangConHieuLuc(id);
        kh.setDaXoa(1);
        khachHangRepository.save(kh);
        auditLogService.ghiLai(ActionType.XOA_KHACH_HANG, "khach_hang", id,
                "Xóa mềm khách hàng " + kh.getHoVaTen());
        log.info("Xóa mềm khách hàng thành công - id={}", id);
    }

    @Transactional(readOnly = true)
    public KhachHangDto layTheoId(Integer id) {
        return toDto(layKhachHangConHieuLuc(id));
    }

    /** Danh sách Trang 3: tìm kiếm + bộ lọc + phân trang. */
    @Transactional(readOnly = true)
    public PageResponse<KhachHangDto> danhSach(String search, String filter, Pageable pageable) {
        String tuKhoa = search == null ? "" : search.trim();
        BoLocKhachHang boLoc = BoLocKhachHang.tuChuoi(filter);

        var trang = switch (boLoc) {
            case KHONG_CO_HO -> khachHangRepository.timKiemKhongCoHo(tuKhoa, pageable);
            case CHUA_MUA_BHYT -> khachHangRepository.timKiemChuaMuaBhyt(tuKhoa, pageable);
            case TAT_CA -> khachHangRepository.timKiem(tuKhoa, pageable);
        };
        return PageResponse.from(trang, this::toDto);
    }

    /** Màn hình chi tiết: khách hàng + lịch sử mua + thành viên cùng hộ. */
    @Transactional(readOnly = true)
    public KhachHangChiTietDto chiTiet(Integer id) {
        KhachHang kh = layKhachHangConHieuLuc(id);

        List<BaoHiemYTeDto> lichSuBhyt = baoHiemYTeRepository.findLichSu(id).stream()
                .map(baoHiemYTeMapper::toDto)
                .toList();

        List<ThanhVienHoGiaDinhDto> cungHo = List.of();
        ThanhVienHoGiaDinh hoHienTai = thanhVienHoGiaDinhRepository.findHoHienTaiCuaKhachHang(id).orElse(null);
        if (hoHienTai != null) {
            Integer idHo = hoHienTai.getHoGiaDinh().getId();
            cungHo = thanhVienHoGiaDinhRepository.findThanhVienDangHoatDong(idHo).stream()
                    .filter(tv -> !tv.getKhachHang().getId().equals(id))
                    .map(tv -> thanhVienMapper.toDto(tv, theHienHanh(tv.getKhachHang().getId())))
                    .toList();
        }

        return KhachHangChiTietDto.builder()
                .khachHang(toDto(kh))
                .lichSuBhyt(lichSuBhyt)
                .thanhVienCungHo(cungHo)
                .build();
    }

    // ------------------------------------------------------------------
    // Hỗ trợ nội bộ
    // ------------------------------------------------------------------

    private KhachHang layKhachHangConHieuLuc(Integer id) {
        KhachHang kh = khachHangRepository.findById(id)
                .orElseThrow(() -> new MyException(ErrorCode.KHACH_HANG_KHONG_TON_TAI, "ID: " + id));
        if (kh.getDaXoa() != null && kh.getDaXoa() == 1) {
            throw new MyException(ErrorCode.KHACH_HANG_DA_XOA, "ID: " + id);
        }
        return kh;
    }

    private void themVaoHo(KhachHang kh, Integer idHo) {
        HoGiaDinh ho = hoGiaDinhRepository.findById(idHo)
                .orElseThrow(() -> new MyException(ErrorCode.HO_GIA_DINH_KHONG_TON_TAI, "ID: " + idHo));
        if (thanhVienHoGiaDinhRepository.findHoHienTaiCuaKhachHang(kh.getId()).isPresent()) {
            throw new MyException(ErrorCode.KHACH_HANG_DA_CO_HO);
        }
        ThanhVienHoGiaDinh tv = new ThanhVienHoGiaDinh();
        tv.setKhachHang(kh);
        tv.setHoGiaDinh(ho);
        tv.setNgayTao(LocalDate.now());
        thanhVienHoGiaDinhRepository.save(tv);
        ho.setSoThanhVien(thanhVienHoGiaDinhRepository.demThanhVienDangHoatDong(idHo));
        hoGiaDinhRepository.save(ho);
    }

    private KhachHangDto toDto(KhachHang kh) {
        ThanhVienHoGiaDinh ho = thanhVienHoGiaDinhRepository.findHoHienTaiCuaKhachHang(kh.getId()).orElse(null);
        return khachHangMapper.toDto(kh, ho, theHienHanh(kh.getId()));
    }

    private BaoHiemYTe theHienHanh(Integer idKhachHang) {
        return baoHiemYTeRepository.findTheHienHanh(idKhachHang).orElse(null);
    }
}

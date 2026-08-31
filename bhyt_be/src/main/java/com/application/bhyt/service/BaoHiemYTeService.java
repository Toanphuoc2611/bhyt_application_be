package com.application.bhyt.service;

import com.application.bhyt.dto.request.CreateBaoHiemYTeRequest;
import com.application.bhyt.dto.request.GiaHanBaoHiemYTeRequest;
import com.application.bhyt.dto.request.UpdateBaoHiemYTeRequest;
import com.application.bhyt.dto.response.BaoHiemYTeDto;
import com.application.bhyt.dto.response.PageResponse;
import com.application.bhyt.entity.BaoHiemYTe;
import com.application.bhyt.entity.KhachHang;
import com.application.bhyt.enums.ActionType;
import com.application.bhyt.enums.ErrorCode;
import com.application.bhyt.enums.LoaiBHYT;
import com.application.bhyt.enums.TrangThaiBHYT;
import com.application.bhyt.exception.MyException;
import com.application.bhyt.mapper.BaoHiemYTeMapper;
import com.application.bhyt.repository.BaoHiemYTeRepository;
import com.application.bhyt.repository.KhachHangRepository;
import com.application.bhyt.util.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Nghiệp vụ thẻ BHYT (Trang 2): tạo mới, gia hạn, cập nhật, xóa mềm, tìm kiếm,
 * xác nhận hoa hồng.
 *
 * <p>Mọi phép tính tiền / hạn thẻ đều ủy quyền cho {@link BaoHiemTinhToanService}
 * - không lặp lại công thức ở đây.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BaoHiemYTeService {

    private final BaoHiemYTeRepository baoHiemYTeRepository;
    private final KhachHangRepository khachHangRepository;
    private final BaoHiemTinhToanService tinhToanService;
    private final BaoHiemYTeMapper baoHiemYTeMapper;
    private final AuditLogService auditLogService;

    /** Tạo thẻ mới ({@code loai = "mới"}). */
    public BaoHiemYTeDto taoMoi(CreateBaoHiemYTeRequest req) {
        KhachHang kh = khachHangRepository.findActiveById(req.getIdKhachHang())
                .orElseThrow(() -> new MyException(ErrorCode.KHACH_HANG_KHONG_TON_TAI, "ID: " + req.getIdKhachHang()));

        BaoHiemYTe bhyt = new BaoHiemYTe();
        bhyt.setKhachHang(kh);
        bhyt.setNgayMua(DateUtils.parse(req.getNgayMua()));
        bhyt.setGoiMacDinh(req.getSoThangMua() == null);
        bhyt.setSoThangMua(req.getSoThangMua()); // null -> apDungCongThuc sẽ quy đổi
        bhyt.setSoLanMuaCuaHo(req.getSoLanMuaCuaHo()); // FE nhập, đã được validate không null
        bhyt.setLoai(LoaiBHYT.MOI.getGiaTri());
        bhyt.setNoiDangKy(req.getNoiDangKy());
        bhyt.setDaNhanHoaHong(0);
        bhyt.setDaXoa(0);
        bhyt.setBhytMoiNhat(1);
        tinhToanService.apDungCongThuc(bhyt);

        chuyenTheCuThanhLichSu(kh.getId());
        bhyt = baoHiemYTeRepository.save(bhyt);

        auditLogService.ghiLai(ActionType.TAO_BHYT, "bao_hiem_y_te", bhyt.getId(),
                "Tạo thẻ mới cho " + kh.getHoVaTen());
        log.info("Tạo thẻ BHYT thành công - id={}, khachHang={}", bhyt.getId(), kh.getHoVaTen());
        return baoHiemYTeMapper.toDto(bhyt);
    }

    /**
     * Gia hạn: tạo dòng mới {@code loai = "gia hạn"}, GIỮ NGUYÊN bậc số lần mua của hộ
     * của thẻ được gia hạn, và lật thẻ hiện hành cũ về {@code bhyt_moi_nhat = 0}.
     */
    public BaoHiemYTeDto giaHan(Integer idThe, GiaHanBaoHiemYTeRequest req) {
        BaoHiemYTe theCu = baoHiemYTeRepository.findActiveById(idThe)
                .orElseThrow(() -> new MyException(ErrorCode.BHYT_KHONG_TON_TAI, "ID: " + idThe));
        KhachHang kh = theCu.getKhachHang();

        LocalDate ngayHetHanCu = req.getNgayHetHanCu() != null
                ? DateUtils.parse(req.getNgayHetHanCu())
                : theCu.getHanThe();

        BaoHiemYTe theMoi = new BaoHiemYTe();
        theMoi.setKhachHang(kh);
        theMoi.setNgayMua(DateUtils.parse(req.getNgayMua()));
        theMoi.setNgayHetHanCu(ngayHetHanCu);
        theMoi.setGoiMacDinh(req.getSoThangMua() == null);
        theMoi.setSoThangMua(req.getSoThangMua());
        theMoi.setSoLanMuaCuaHo(theCu.getSoLanMuaCuaHo()); // gia hạn không phải lần mua mới của hộ
        theMoi.setLoai(LoaiBHYT.GIA_HAN.getGiaTri());
        theMoi.setNoiDangKy(req.getNoiDangKy() != null ? req.getNoiDangKy() : theCu.getNoiDangKy());
        theMoi.setDaNhanHoaHong(0);
        theMoi.setDaXoa(0);
        theMoi.setBhytMoiNhat(1);
        tinhToanService.apDungCongThuc(theMoi);

        chuyenTheCuThanhLichSu(kh.getId());
        theMoi = baoHiemYTeRepository.save(theMoi);

        auditLogService.ghiLai(ActionType.GIA_HAN_BHYT, "bao_hiem_y_te", theMoi.getId(),
                "Gia hạn thẻ #" + idThe + " cho " + kh.getHoVaTen());
        log.info("Gia hạn thẻ BHYT thành công - thẻ cũ={}, thẻ mới={}", idThe, theMoi.getId());
        return baoHiemYTeMapper.toDto(theMoi);
    }

    /** Cập nhật thẻ; tính lại {@code hanThe} và {@code soTienThu}. */
    public BaoHiemYTeDto capNhat(Integer id, UpdateBaoHiemYTeRequest req) {
        BaoHiemYTe bhyt = layTheConHieuLuc(id);

        if (req.getNgayMua() != null) bhyt.setNgayMua(DateUtils.parse(req.getNgayMua()));
        if (req.getNgayHetHanCu() != null) bhyt.setNgayHetHanCu(DateUtils.parse(req.getNgayHetHanCu()));
        if (req.getGoiMacDinh() != null) bhyt.setGoiMacDinh(req.getGoiMacDinh());
        if (req.getSoThangMua() != null) {
            bhyt.setSoThangMua(req.getSoThangMua());
            bhyt.setGoiMacDinh(Boolean.FALSE);
        }
        if (req.getSoLanMuaCuaHo() != null) bhyt.setSoLanMuaCuaHo(req.getSoLanMuaCuaHo());
        if (req.getNoiDangKy() != null) bhyt.setNoiDangKy(req.getNoiDangKy());

        tinhToanService.apDungCongThuc(bhyt);
        bhyt = baoHiemYTeRepository.save(bhyt);

        auditLogService.ghiLai(ActionType.CAP_NHAT_BHYT, "bao_hiem_y_te", bhyt.getId(),
                "Cập nhật thẻ cho " + bhyt.getKhachHang().getHoVaTen());
        log.info("Cập nhật thẻ BHYT thành công - id={}", id);
        return baoHiemYTeMapper.toDto(bhyt);
    }

    /** Xóa mềm thẻ (da_xoa = 1). */
    public void xoaMem(Integer id) {
        BaoHiemYTe bhyt = layTheConHieuLuc(id);
        bhyt.setDaXoa(1);
        baoHiemYTeRepository.save(bhyt);
        auditLogService.ghiLai(ActionType.XOA_BHYT, "bao_hiem_y_te", id,
                "Xóa mềm thẻ của " + bhyt.getKhachHang().getHoVaTen());
        log.info("Xóa mềm thẻ BHYT thành công - id={}", id);
    }

    @Transactional(readOnly = true)
    public BaoHiemYTeDto layTheoId(Integer id) {
        return baoHiemYTeMapper.toDto(layTheConHieuLuc(id));
    }

    /** Danh sách Trang 2: tìm kiếm theo tên/CCCD + lọc trạng thái + phân trang. */
    @Transactional(readOnly = true)
    public PageResponse<BaoHiemYTeDto> danhSach(String search, String trangThai, Pageable pageable) {
        String tuKhoa = search == null ? "" : search.trim();
        TrangThaiBHYT tt = TrangThaiBHYT.tuChuoi(trangThai);
        LocalDate homNay = LocalDate.now();

        var trang = switch (tt) {
            case HET_HAN -> baoHiemYTeRepository.locHetHan(tuKhoa, homNay, pageable);
            case CHUA_NHAN_HOA_HONG -> baoHiemYTeRepository.locChuaNhanHoaHong(tuKhoa, pageable);
            case GAN_HET_HAN_7, GAN_HET_HAN_14, GAN_HET_HAN_30, GAN_HET_HAN_60 ->
                    baoHiemYTeRepository.locGanHetHan(tuKhoa, homNay, homNay.plusDays(tt.soNgayGanHetHan()), pageable);
            case TAT_CA -> baoHiemYTeRepository.locTatCa(tuKhoa, pageable);
        };
        return PageResponse.from(trang, baoHiemYTeMapper::toDto);
    }

    /** Xác nhận đã nhận hoa hồng hàng loạt. */
    public void xacNhanHoaHong(List<Integer> ids) {
        List<BaoHiemYTe> danhSach = ids.stream().map(this::layTheConHieuLuc).toList();
        danhSach.forEach(b -> b.setDaNhanHoaHong(1));
        baoHiemYTeRepository.saveAll(danhSach);
        ids.forEach(id -> auditLogService.ghiLai(ActionType.XAC_NHAN_HOA_HONG, "bao_hiem_y_te", id,
                "Xác nhận đã nhận hoa hồng"));
        log.info("Xác nhận nhận hoa hồng thành công - {} thẻ", danhSach.size());
    }

    // ------------------------------------------------------------------

    private BaoHiemYTe layTheConHieuLuc(Integer id) {
        BaoHiemYTe bhyt = baoHiemYTeRepository.findById(id)
                .orElseThrow(() -> new MyException(ErrorCode.BHYT_KHONG_TON_TAI, "ID: " + id));
        if (bhyt.getDaXoa() != null && bhyt.getDaXoa() == 1) {
            throw new MyException(ErrorCode.BHYT_DA_XOA, "ID: " + id);
        }
        return bhyt;
    }

    /** Lật thẻ hiện hành hiện tại của khách hàng (nếu có) thành thẻ cũ. */
    private void chuyenTheCuThanhLichSu(Integer idKhachHang) {
        baoHiemYTeRepository.findTheHienHanh(idKhachHang).ifPresent(theCu -> {
            theCu.setBhytMoiNhat(0);
            baoHiemYTeRepository.save(theCu);
        });
    }
}

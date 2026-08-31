package com.application.bhyt.service;

import com.application.bhyt.config.BhytProperties;
import com.application.bhyt.entity.BaoHiemYTe;
import com.application.bhyt.entity.HoGiaDinh;
import com.application.bhyt.entity.KhachHang;
import com.application.bhyt.entity.ThanhVienHoGiaDinh;
import com.application.bhyt.enums.ActionType;
import com.application.bhyt.enums.LoaiBHYT;
import com.application.bhyt.repository.BaoHiemYTeRepository;
import com.application.bhyt.repository.HoGiaDinhRepository;
import com.application.bhyt.repository.KhachHangRepository;
import com.application.bhyt.repository.ThanhVienHoGiaDinhRepository;
import com.application.bhyt.util.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Sao lưu TOÀN BỘ dữ liệu ra một file Excel, được gọi mỗi lần server khởi động
 * (và sau khi nhập Excel). File: {@code <bhyt.backup.dir>/bhyt_backup_yyyyMMdd_HHmmss.xlsx}
 * - luôn tạo mới, không ghi đè, để giữ lịch sử.
 *
 * <p><b>Định dạng giống hệt file "DS Mua BHYT" của khách hàng</b> để có thể mở đọc
 * quen thuộc VÀ nạp ngược lại vào DB bằng {@code --nhap-excel=<file backup>}:</p>
 * <ul>
 *   <li>Sheet <b>Danh_sach</b>: dòng 1 tiêu đề, dòng 2 header, 18 cột đúng thứ tự;
 *       các dòng nhóm theo hộ (tên hộ ở dòng đầu, để trống các dòng sau);
 *       cột "Thành tiền" / "Hạn thẻ" là <b>công thức Excel thật</b> (tham chiếu ô cấu hình
 *       Z1 = lương cơ sở, AA1, AB1) kèm sẵn giá trị đã tính.</li>
 *   <li>Sheet <b>Het_han</b>: cùng định dạng, chỉ gồm thẻ đã hết hạn ({@code han_the < hôm nay}).</li>
 *   <li>4 sheet raw <b>_khach_hang, _bao_hiem_y_te, _ho_gia_dinh, _thanh_vien</b>:
 *       dump đầy đủ từng bảng, đảm bảo không mất dữ liệu (lịch sử thẻ, thành viên đã rời hộ...).</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SaoLuuExcelService {

    private static final DateTimeFormatter MOC_THOI_GIAN = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final String TIEU_DE = "DANH SÁCH MUA BẢO HIỂM Y TẾ";
    private static final String[] HEADER = {
            "STT", "Hộ", "Họ và tên", "Năm sinh", "Địa chỉ", "Ngày mua", "CCCD", "Loại", "Nơi ĐK",
            "Mstb", "Tháng mua", "Người thứ", "Thành tiền", "Ngày hết hạn", "Hạn thẻ", "Liên hệ", "SĐT", "Ghi Chú"
    };
    // Chỉ số cột 0-based
    private static final int C_STT = 0, C_HO = 1, C_TEN = 2, C_NAM_SINH = 3, C_DIA_CHI = 4, C_NGAY_MUA = 5,
            C_CCCD = 6, C_LOAI = 7, C_NOI_DK = 8, C_MSTB = 9, C_THANG_MUA = 10, C_NGUOI_THU = 11,
            C_THANH_TIEN = 12, C_NGAY_HET_HAN = 13, C_HAN_THE = 14, C_LIEN_HE = 15, C_SDT = 16, C_GHI_CHU = 17;
    // Ô cấu hình
    private static final int C_CAU_HINH_NHAN = 24;  // Y
    private static final int C_LUONG_CO_SO = 25;     // Z1
    private static final int C_AA = 26;             // AA1 = Z1 * m
    private static final int C_AB = 27;             // AB1 = Z1 * m * số tháng mặc định

    private final BhytProperties properties;
    private final KhachHangRepository khachHangRepository;
    private final BaoHiemYTeRepository baoHiemYTeRepository;
    private final HoGiaDinhRepository hoGiaDinhRepository;
    private final ThanhVienHoGiaDinhRepository thanhVienHoGiaDinhRepository;
    private final AuditLogService auditLogService;

    // ------------------------------------------------------------------

    /** Một dòng của sheet Danh_sach / Het_han. */
    private record Dong(String tenHo, boolean dauHo, KhachHang kh, BaoHiemYTe the) {
    }

    @Transactional(readOnly = true)
    public Path saoLuu() throws Exception {
        Path thuMuc = Path.of(properties.getBackup().getDir());
        Files.createDirectories(thuMuc);
        Path file = thuMuc.resolve("bhyt_backup_" + LocalDateTime.now().format(MOC_THOI_GIAN) + ".xlsx");

        List<Dong> danhSach = xayDungDanhSach();
        int soCongThucLoi = 0;

        try (SXSSFWorkbook wb = new SXSSFWorkbook(100); OutputStream out = Files.newOutputStream(file)) {
            wb.setForceFormulaRecalculation(true);
            KieuO kieu = new KieuO(wb);

            soCongThucLoi += ghiSheetDinhDangGoc(wb, kieu, "Danh_sach", danhSach, false);

            LocalDate homNay = LocalDate.now();
            List<Dong> hetHan = danhSach.stream()
                    .filter(d -> d.the() != null && d.the().getHanThe() != null && d.the().getHanThe().isBefore(homNay))
                    .toList();
            soCongThucLoi += ghiSheetDinhDangGoc(wb, kieu, "Het_han", hetHan, true);

            ghiSheetRawKhachHang(wb);
            ghiSheetRawBaoHiem(wb);
            ghiSheetRawHoGiaDinh(wb);
            ghiSheetRawThanhVien(wb);

            wb.write(out);
        }

        if (soCongThucLoi > 0) {
            log.warn("Có {} ô không ghi được công thức Excel, đã ghi giá trị số thay thế", soCongThucLoi);
        }
        auditLogService.ghiLai(ActionType.SAO_LUU_EXCEL, "-", null, "Sao lưu toàn bộ dữ liệu -> " + file);
        log.info("Sao lưu dữ liệu thành công: {}", file.toAbsolutePath());
        return file;
    }

    // ------------------------------------------------------------------
    // Dựng danh sách dòng: nhóm theo hộ, rồi tới khách hàng không thuộc hộ nào
    // ------------------------------------------------------------------
    private List<Dong> xayDungDanhSach() {
        List<Dong> dsRes = new ArrayList<>();

        for (HoGiaDinh ho : hoGiaDinhRepository.findAll(Sort.by("id"))) {
            List<ThanhVienHoGiaDinh> tvs = thanhVienHoGiaDinhRepository.findThanhVienDangHoatDong(ho.getId());
            if (tvs.isEmpty()) continue;
            String tenHo = ho.getTen() != null && !ho.getTen().isBlank()
                    ? ho.getTen()
                    : tvs.get(0).getKhachHang().getHoVaTen();
            boolean dau = true;
            for (ThanhVienHoGiaDinh tv : tvs) {
                KhachHang kh = tv.getKhachHang();
                BaoHiemYTe the = baoHiemYTeRepository.findTheHienHanh(kh.getId()).orElse(null);
                dsRes.add(new Dong(tenHo, dau, kh, the));
                dau = false;
            }
        }

        // Khách hàng không thuộc hộ nào -> ghi cột "Hộ" = tên họ (nhập lại sẽ thành hộ 1 người)
        for (KhachHang kh : khachHangRepository.findAllActive()) {
            if (thanhVienHoGiaDinhRepository.findHoHienTaiCuaKhachHang(kh.getId()).isEmpty()) {
                BaoHiemYTe the = baoHiemYTeRepository.findTheHienHanh(kh.getId()).orElse(null);
                dsRes.add(new Dong(kh.getHoVaTen(), true, kh, the));
            }
        }
        return dsRes;
    }

    // ------------------------------------------------------------------
    // Ghi một sheet theo định dạng file gốc
    // ------------------------------------------------------------------
    private int ghiSheetDinhDangGoc(SXSSFWorkbook wb, KieuO kieu, String tenSheet, List<Dong> rows, boolean hienTenHoMoiDong) {
        Sheet sheet = wb.createSheet(tenSheet);

        // Dòng 1: tiêu đề (gộp ô) + ô cấu hình
        Row r1 = sheet.createRow(0);
        Cell tieuDe = r1.createCell(C_STT);
        tieuDe.setCellValue(TIEU_DE);
        tieuDe.setCellStyle(kieu.tieuDe);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, C_STT, C_GHI_CHU));

        double m = properties.getMucDong();
        int soThangMD = properties.getSoThangMacDinh();
        r1.createCell(C_CAU_HINH_NHAN).setCellValue("Lương cơ sở →");
        Cell z1 = r1.createCell(C_LUONG_CO_SO);
        z1.setCellValue(properties.getLuongCoSo());
        Cell aa1 = r1.createCell(C_AA);
        datCongThucSo(aa1, "Z1*" + so(m), properties.getLuongCoSo() * m);
        Cell ab1 = r1.createCell(C_AB);
        datCongThucSo(ab1, "Z1*" + so(m) + "*" + soThangMD, properties.getLuongCoSo() * m * soThangMD);

        // Dòng 2: header
        Row r2 = sheet.createRow(1);
        for (int i = 0; i < HEADER.length; i++) {
            Cell c = r2.createCell(i);
            c.setCellValue(HEADER[i]);
            c.setCellStyle(kieu.header);
        }

        int loiCongThuc = 0;
        int stt = 1;
        for (int idx = 0; idx < rows.size(); idx++) {
            Dong d = rows.get(idx);
            int er = idx + 3;                 // số dòng Excel (1-based): dữ liệu bắt đầu ở dòng 3
            Row row = sheet.createRow(er - 1);
            KhachHang kh = d.kh();
            BaoHiemYTe t = d.the();

            oSo(row, C_STT, stt++);
            if (d.dauHo() || hienTenHoMoiDong) oChuoi(row, C_HO, d.tenHo());
            oChuoi(row, C_TEN, kh.getHoVaTen());
            oNgay(row, C_NAM_SINH, kh.getNgaySinh(), kieu.ngay);
            oChuoi(row, C_DIA_CHI, kh.getDiaChi());
            oChuoi(row, C_CCCD, kh.getCccd());
            oChuoi(row, C_LIEN_HE, kh.getLienLacKhac());
            oChuoi(row, C_SDT, kh.getSoDienThoai());
            oChuoi(row, C_GHI_CHU, kh.getGhiChu());
            oChuoi(row, C_MSTB, kh.getCoMstb() != null && kh.getCoMstb() == 1 ? "mstb" : null);

            if (t != null) {
                oNgay(row, C_NGAY_MUA, t.getNgayMua(), kieu.ngay);
                oChuoi(row, C_LOAI, LoaiBHYT.GIA_HAN.getGiaTri().equalsIgnoreCase(t.getLoai()) ? "Gia hạn" : null);
                oChuoi(row, C_NOI_DK, t.getNoiDangKy());
                boolean goiMacDinh = Boolean.TRUE.equals(t.getGoiMacDinh());
                oChuoi(row, C_THANG_MUA, goiMacDinh || t.getSoThangMua() == null ? null : t.getSoThangMua() + " tháng");
                oSo(row, C_NGUOI_THU, t.getSoLanMuaCuaHo() != null ? t.getSoLanMuaCuaHo() : 1);
                oNgay(row, C_NGAY_HET_HAN, t.getNgayHetHanCu(), kieu.ngay);

                Cell cTien = row.createCell(C_THANH_TIEN);
                cTien.setCellStyle(kieu.tien);
                if (!datCongThucSo(cTien, congThucThanhTien(er), t.getSoTienThu() != null ? t.getSoTienThu() : 0)) loiCongThuc++;

                Cell cHan = row.createCell(C_HAN_THE);
                cHan.setCellStyle(kieu.ngay);
                if (!datCongThucNgay(cHan, congThucHanThe(er), t.getHanThe())) loiCongThuc++;
            }
        }

        for (int i = 0; i < HEADER.length; i++) sheet.setColumnWidth(i, 14 * 256);
        sheet.setColumnWidth(C_TEN, 24 * 256);
        sheet.setColumnWidth(C_DIA_CHI, 20 * 256);
        return loiCongThuc;
    }

    // ------------------------------------------------------------------
    // Công thức Excel giống hệt file gốc
    // ------------------------------------------------------------------
    private String congThucThanhTien(int er) {
        double t2 = properties.tyLeChoBac(2), t3 = properties.tyLeChoBac(3),
                t4 = properties.tyLeChoBac(4), t5 = properties.tyLeChoBac(5), tm = properties.getTyLeMstb();
        String J = "J" + er, K = "K" + er, L = "L" + er;
        // n * AA1  (n = số tháng trích từ ô "Tháng mua")
        String nAA = "VALUE(LEFT(" + K + ",SEARCH(\" \"," + K + ")-1))*$AA$1";
        String bacAB = "IF(" + L + "=1,$AB$1,IF(" + L + "=2,$AB$1*" + so(t2) + ",IF(" + L + "=3,$AB$1*" + so(t3)
                + ",IF(" + L + "=4,$AB$1*" + so(t4) + ",$AB$1*" + so(t5) + "))))";
        String bacN = "IF(" + L + "=1," + nAA + ",IF(" + L + "=2," + nAA + "*" + so(t2) + ",IF(" + L + "=3," + nAA + "*" + so(t3)
                + ",IF(" + L + "=4," + nAA + "*" + so(t4) + "," + nAA + "*" + so(t5) + "))))";
        return "IF(AND(" + J + "=\"\"," + K + "=\"\")," + bacAB
                + ",IF(AND(" + J + "=\"\"," + K + "<>\"\")," + bacN
                + ",IF(AND(" + J + "=\"mstb\"," + K + "=\"\"),$AB$1*" + so(tm) + "," + nAA + "*" + so(tm) + ")))";
    }

    private String congThucHanThe(int er) {
        int thr = properties.getGiaHanNguongNgayMuaSom();
        int defM = properties.getSoThangMacDinh();
        int defMPlus = defM + properties.getTheMoiCongThemThang();
        int plusDays = properties.getTheMoiCongThemNgay();
        String F = "F" + er, H = "H" + er, N = "N" + er, K = "K" + er;
        String nThang = "VALUE(LEFT(" + K + ",SEARCH(\" \"," + K + ",1)))";
        return "IF(" + H + "=\"Gia hạn\","
                + "IF(" + F + "-" + N + ">=-" + thr + ","
                + "IF(" + K + "=\"\",EDATE(" + N + "," + defM + "),EDATE(" + N + "," + nThang + ")),"
                + N + "),"
                + "IF(" + K + "=\"\",EDATE(" + F + "," + defMPlus + "),EDATE(" + F + "," + nThang + ")+" + plusDays + "))";
    }

    /** In số không có dấu phẩy nhóm, bỏ ".0" thừa. */
    private String so(double d) {
        if (d == Math.rint(d)) return String.valueOf((long) d);
        return String.valueOf(d);
    }

    // ------------------------------------------------------------------
    // Ghi ô: công thức + giá trị cache; nếu POI không parse được thì chỉ ghi giá trị
    // ------------------------------------------------------------------
    private boolean datCongThucSo(Cell cell, String congThuc, double giaTri) {
        try {
            cell.setCellFormula(congThuc);
            cell.setCellValue(giaTri);
            return true;
        } catch (Exception e) {
            cell.setCellValue(giaTri);
            return false;
        }
    }

    private boolean datCongThucNgay(Cell cell, String congThuc, LocalDate giaTri) {
        try {
            cell.setCellFormula(congThuc);
            if (giaTri != null) cell.setCellValue(giaTri);
            return true;
        } catch (Exception e) {
            if (giaTri != null) cell.setCellValue(giaTri);
            return false;
        }
    }

    private void oChuoi(Row row, int c, String v) {
        if (v != null && !v.isBlank()) row.createCell(c).setCellValue(v);
    }

    private void oSo(Row row, int c, Integer v) {
        if (v != null) row.createCell(c).setCellValue(v);
    }

    private void oNgay(Row row, int c, LocalDate v, CellStyle kieuNgay) {
        if (v == null) return;
        Cell cell = row.createCell(c);
        cell.setCellValue(v);
        cell.setCellStyle(kieuNgay);
    }

    // ------------------------------------------------------------------
    // Các sheet raw (dump nguyên bảng)
    // ------------------------------------------------------------------
    private void ghiSheetRawKhachHang(SXSSFWorkbook wb) {
        Sheet sheet = wb.createSheet("_khach_hang");
        rawHeader(sheet, "id", "cccd", "ho_va_ten", "ngay_sinh", "dia_chi", "so_dien_thoai",
                "lien_lac_khac", "hinh_anh", "ghi_chu", "bhyt_khac", "co_mstb", "da_xoa", "ngay_tao", "ngay_cap_nhat");
        int r = 1;
        for (KhachHang k : khachHangRepository.findAll()) {
            rawRow(sheet.createRow(r++), k.getId(), k.getCccd(), k.getHoVaTen(), DateUtils.format(k.getNgaySinh()),
                    k.getDiaChi(), k.getSoDienThoai(), k.getLienLacKhac(), k.getHinhAnh(), k.getGhiChu(),
                    k.getBhytKhac(), k.getCoMstb(), k.getDaXoa(),
                    DateUtils.format(k.getNgayTao()), DateUtils.format(k.getNgayCapNhat()));
        }
    }

    private void ghiSheetRawBaoHiem(SXSSFWorkbook wb) {
        Sheet sheet = wb.createSheet("_bao_hiem_y_te");
        rawHeader(sheet, "id", "id_khach_hang", "ngay_mua", "ngay_het_han_cu", "so_thang_mua", "goi_mac_dinh",
                "han_the", "so_lan_mua_cua_ho", "so_tien_thu", "da_nhan_hoa_hong", "bhyt_moi_nhat", "da_xoa",
                "loai", "noi_dang_ky", "ngay_tao", "ngay_cap_nhat");
        int r = 1;
        for (BaoHiemYTe b : baoHiemYTeRepository.findAll()) {
            rawRow(sheet.createRow(r++), b.getId(), b.getKhachHang() != null ? b.getKhachHang().getId() : null,
                    DateUtils.format(b.getNgayMua()), DateUtils.format(b.getNgayHetHanCu()), b.getSoThangMua(),
                    b.getGoiMacDinh(), DateUtils.format(b.getHanThe()), b.getSoLanMuaCuaHo(), b.getSoTienThu(),
                    b.getDaNhanHoaHong(), b.getBhytMoiNhat(), b.getDaXoa(), b.getLoai(), b.getNoiDangKy(),
                    DateUtils.format(b.getNgayTao()), DateUtils.format(b.getNgayCapNhat()));
        }
    }

    private void ghiSheetRawHoGiaDinh(SXSSFWorkbook wb) {
        Sheet sheet = wb.createSheet("_ho_gia_dinh");
        rawHeader(sheet, "id", "ten", "so_thanh_vien", "hinh_anh", "ngay_tao", "ngay_cap_nhat");
        int r = 1;
        for (HoGiaDinh h : hoGiaDinhRepository.findAll()) {
            rawRow(sheet.createRow(r++), h.getId(), h.getTen(), h.getSoThanhVien(), h.getHinhAnh(),
                    DateUtils.format(h.getNgayTao()), DateUtils.format(h.getNgayCapNhat()));
        }
    }

    private void ghiSheetRawThanhVien(SXSSFWorkbook wb) {
        Sheet sheet = wb.createSheet("_thanh_vien");
        rawHeader(sheet, "id", "id_thanh_vien", "id_ho_gia_dinh", "ngay_tao", "ngay_ket_thuc");
        int r = 1;
        for (ThanhVienHoGiaDinh t : thanhVienHoGiaDinhRepository.findAll()) {
            rawRow(sheet.createRow(r++), t.getId(),
                    t.getKhachHang() != null ? t.getKhachHang().getId() : null,
                    t.getHoGiaDinh() != null ? t.getHoGiaDinh().getId() : null,
                    DateUtils.format(t.getNgayTao()), DateUtils.format(t.getNgayKetThuc()));
        }
    }

    private void rawHeader(Sheet sheet, String... ten) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < ten.length; i++) row.createCell(i).setCellValue(ten[i]);
    }

    private void rawRow(Row row, Object... giaTri) {
        for (int i = 0; i < giaTri.length; i++) {
            Object v = giaTri[i];
            if (v == null) row.createCell(i).setBlank();
            else if (v instanceof Number n) row.createCell(i).setCellValue(n.doubleValue());
            else if (v instanceof Boolean b) row.createCell(i).setCellValue(b);
            else row.createCell(i).setCellValue(String.valueOf(v));
        }
    }

    // ------------------------------------------------------------------
    /** Các kiểu ô dùng chung trong workbook. */
    private static final class KieuO {
        final CellStyle tieuDe;
        final CellStyle header;
        final CellStyle ngay;
        final CellStyle tien;

        KieuO(SXSSFWorkbook wb) {
            DataFormat fmt = wb.createDataFormat();

            Font fTieuDe = wb.createFont();
            fTieuDe.setBold(true);
            fTieuDe.setFontHeightInPoints((short) 14);
            tieuDe = wb.createCellStyle();
            tieuDe.setFont(fTieuDe);
            tieuDe.setAlignment(HorizontalAlignment.CENTER);

            Font fHeader = wb.createFont();
            fHeader.setBold(true);
            header = wb.createCellStyle();
            header.setFont(fHeader);
            header.setAlignment(HorizontalAlignment.CENTER);
            header.setWrapText(true);

            ngay = wb.createCellStyle();
            ngay.setDataFormat(fmt.getFormat("dd/mm/yyyy"));

            tien = wb.createCellStyle();
            tien.setDataFormat(fmt.getFormat("#,##0"));
        }
    }
}

package com.application.bhyt.service;

import com.application.bhyt.dto.response.NhapExcelKetQua;
import com.application.bhyt.entity.BaoHiemYTe;
import com.application.bhyt.entity.HoGiaDinh;
import com.application.bhyt.entity.KhachHang;
import com.application.bhyt.entity.ThanhVienHoGiaDinh;
import com.application.bhyt.enums.LoaiBHYT;
import com.application.bhyt.repository.BaoHiemYTeRepository;
import com.application.bhyt.repository.HoGiaDinhRepository;
import com.application.bhyt.repository.KhachHangRepository;
import com.application.bhyt.repository.ThanhVienHoGiaDinhRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Nhập dữ liệu từ file Excel "DS Mua BHYT" (.xlsm) vào cơ sở dữ liệu.
 *
 * <p>Theo yêu cầu: <b>mỗi lần nhập sẽ XÓA SẠCH 4 bảng rồi nạp lại</b> - file Excel
 * là nguồn sự thật duy nhất. Chỉ nên chạy bằng công cụ dòng lệnh
 * ({@code --nhap-excel=<đường dẫn>}), không phải khi server đang phục vụ.</p>
 *
 * <h3>Ánh xạ cột (sheet "Danh_sach", tiêu đề ở dòng 2)</h3>
 * <pre>
 * Hộ          -> ho_gia_dinh.ten (ô trống = cùng hộ với dòng ngay trên)
 * Họ và tên   -> khach_hang.ho_va_ten   (bắt buộc, trống thì bỏ dòng)
 * Năm sinh    -> khach_hang.ngay_sinh   (ô chứa ngày đầy đủ)
 * Địa chỉ     -> khach_hang.dia_chi
 * CCCD        -> khach_hang.cccd        (trống -> null)
 * Mstb        -> khach_hang.co_mstb     ("mstb" -> 1, ngược lại 0)
 * Liên hệ     -> khach_hang.lien_lac_khac
 * SĐT         -> khach_hang.so_dien_thoai
 * Ghi Chú     -> khach_hang.ghi_chu
 * Ngày mua    -> bao_hiem_y_te.ngay_mua
 * Loại        -> bao_hiem_y_te.loai     ("Gia hạn" -> "gia hạn", còn lại "mới")
 * Nơi ĐK      -> bao_hiem_y_te.noi_dang_ky
 * Tháng mua   -> số tháng ("6 tháng" -> 6; trống -> gói mặc định)
 * Người thứ   -> bao_hiem_y_te.so_lan_mua_cua_ho (trống -> 1)
 * Ngày hết hạn-> bao_hiem_y_te.ngay_het_han_cu (mốc tính hạn thẻ khi gia hạn)
 * </pre>
 * {@code han_the} và {@code so_tien_thu} được TÍNH LẠI bằng {@link BaoHiemTinhToanService};
 * nếu lệch với giá trị trong sheet ("Hạn thẻ" / "Thành tiền") thì ghi vào phần cảnh báo.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NhapExcelService {

    private static final String TEN_SHEET = "Danh_sach";
    private static final int DONG_TIEU_DE = 1;   // 0-based: dòng 2 trong Excel
    private static final Pattern SO_DAU = Pattern.compile("^\\s*(\\d+)");

    private final KhachHangRepository khachHangRepository;
    private final HoGiaDinhRepository hoGiaDinhRepository;
    private final ThanhVienHoGiaDinhRepository thanhVienHoGiaDinhRepository;
    private final BaoHiemYTeRepository baoHiemYTeRepository;
    private final BaoHiemTinhToanService tinhToanService;

    @Transactional
    public NhapExcelKetQua nhapTuFile(Path file) throws Exception {
        NhapExcelKetQua kq = new NhapExcelKetQua();

        // 1) Xóa sạch (thứ tự phụ thuộc khóa ngoại)
        thanhVienHoGiaDinhRepository.deleteAllInBatch();
        baoHiemYTeRepository.deleteAllInBatch();
        hoGiaDinhRepository.deleteAllInBatch();
        khachHangRepository.deleteAllInBatch();
        log.info("Đã xóa sạch dữ liệu cũ, bắt đầu nhập từ {}", file);

        try (Workbook wb = WorkbookFactory.create(file.toFile())) {
            Sheet sheet = wb.getSheet(TEN_SHEET);
            if (sheet == null) sheet = wb.getSheetAt(0);

            Map<String, Integer> cot = docTieuDe(sheet.getRow(DONG_TIEU_DE));
            DataFormatter df = new DataFormatter();

            HoGiaDinh hoHienTai = null;

            for (int i = DONG_TIEU_DE + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String hoVaTen = chuoi(row, cot.get("ho va ten"), df);
                String hoText = chuoi(row, cot.get("ho"), df);

                // Bắt đầu hộ mới khi cột "Hộ" có giá trị
                if (hoText != null && !hoText.isBlank()) {
                    hoHienTai = new HoGiaDinh();
                    hoHienTai.setTen(hoText.trim());
                    hoHienTai.setSoThanhVien(0);
                    hoHienTai = hoGiaDinhRepository.save(hoHienTai);
                    kq.setSoHoGiaDinh(kq.getSoHoGiaDinh() + 1);
                }

                LocalDate ngayMua = ngay(row, cot.get("ngay mua"), df);

                if (hoVaTen == null || hoVaTen.isBlank()) {
                    // dòng trắng thật sự -> bỏ qua im lặng; có dữ liệu nhưng thiếu tên -> báo
                    if (ngayMua != null || chuoi(row, cot.get("cccd"), df) != null) {
                        kq.getBoQua().add("Dòng " + (i + 1) + ": thiếu Họ và tên");
                    }
                    continue;
                }

                // 2) Khách hàng
                KhachHang kh = new KhachHang();
                kh.setHoVaTen(hoVaTen.trim());
                kh.setNgaySinh(ngay(row, cot.get("nam sinh"), df));
                kh.setDiaChi(chuoi(row, cot.get("dia chi"), df));
                kh.setCccd(rongThanhNull(chuoi(row, cot.get("cccd"), df)));
                kh.setSoDienThoai(chuoi(row, cot.get("sdt"), df));
                kh.setLienLacKhac(chuoi(row, cot.get("lien he"), df));
                kh.setGhiChu(chuoi(row, cot.get("ghi chu"), df));
                String mstb = chuoi(row, cot.get("mstb"), df);
                boolean coMstb = mstb != null && mstb.trim().equalsIgnoreCase("mstb");
                kh.setCoMstb(coMstb ? 1 : 0);
                kh.setDaXoa(0);
                kh = khachHangRepository.save(kh);
                kq.setSoKhachHang(kq.getSoKhachHang() + 1);

                // 3) Gắn vào hộ hiện tại
                if (hoHienTai != null) {
                    ThanhVienHoGiaDinh tv = new ThanhVienHoGiaDinh();
                    tv.setKhachHang(kh);
                    tv.setHoGiaDinh(hoHienTai);
                    tv.setNgayTao(ngayMua != null ? ngayMua : LocalDate.now());
                    thanhVienHoGiaDinhRepository.save(tv);
                    hoHienTai.setSoThanhVien(hoHienTai.getSoThanhVien() + 1);
                    hoGiaDinhRepository.save(hoHienTai);
                }

                // 4) Thẻ BHYT (chỉ tạo khi có Ngày mua)
                if (ngayMua == null) {
                    kq.getBoQua().add("Dòng " + (i + 1) + " (" + hoVaTen + "): chỉ nhập khách hàng, không có thẻ BHYT");
                    continue;
                }
                Integer soThang = soThangMua(chuoi(row, cot.get("thang mua"), df));
                String loaiRaw = chuoi(row, cot.get("loai"), df);
                boolean laGiaHan = loaiRaw != null && loaiRaw.trim().equalsIgnoreCase("gia hạn");
                Integer nguoiThu = nguyen(row, cot.get("nguoi thu"), df);

                BaoHiemYTe bhyt = new BaoHiemYTe();
                bhyt.setKhachHang(kh);
                bhyt.setNgayMua(ngayMua);
                bhyt.setNgayHetHanCu(ngay(row, cot.get("ngay het han"), df));
                bhyt.setGoiMacDinh(soThang == null);
                bhyt.setSoThangMua(soThang);
                bhyt.setSoLanMuaCuaHo(nguoiThu != null && nguoiThu > 0 ? nguoiThu : 1);
                bhyt.setLoai(laGiaHan ? LoaiBHYT.GIA_HAN.getGiaTri() : LoaiBHYT.MOI.getGiaTri());
                bhyt.setNoiDangKy(chuoi(row, cot.get("noi dk"), df));
                bhyt.setDaNhanHoaHong(0);
                bhyt.setBhytMoiNhat(1);
                bhyt.setDaXoa(0);
                tinhToanService.apDungCongThuc(bhyt);
                bhyt = baoHiemYTeRepository.save(bhyt);
                kq.setSoThe(kq.getSoThe() + 1);

                // 5) Đối chiếu với giá trị trong sheet
                doiChieu(kq, i + 1, hoVaTen, bhyt, row, cot, df);
            }
        }

        log.info("Nhập Excel xong: {} hộ, {} khách hàng, {} thẻ, {} cảnh báo",
                kq.getSoHoGiaDinh(), kq.getSoKhachHang(), kq.getSoThe(), kq.getCanhBao().size());
        return kq;
    }

    // ------------------------------------------------------------------
    // Đọc tiêu đề -> map "tên cột đã chuẩn hóa" -> chỉ số cột
    // ------------------------------------------------------------------
    private Map<String, Integer> docTieuDe(Row header) {
        Map<String, Integer> map = new HashMap<>();
        if (header == null) return map;
        DataFormatter df = new DataFormatter();
        for (int c = header.getFirstCellNum(); c < header.getLastCellNum(); c++) {
            String ten = chuanHoa(df.formatCellValue(header.getCell(c)));
            if (!ten.isBlank()) map.putIfAbsent(ten, c);
        }
        return map;
    }

    /** Bỏ dấu tiếng Việt, gộp khoảng trắng/xuống dòng, viết thường. */
    private String chuanHoa(String s) {
        if (s == null) return "";
        String x = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd').replace('Đ', 'D')
                .toLowerCase()
                .replaceAll("\\s+", " ")
                .trim();
        return x;
    }

    private String chuoi(Row row, Integer c, DataFormatter df) {
        if (c == null || row == null) return null;
        Cell cell = row.getCell(c);
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC && !DateUtil.isCellDateFormatted(cell)) {
            double d = cell.getNumericCellValue();
            if (d == Math.rint(d)) return String.valueOf((long) d);
            return String.valueOf(d);
        }
        String v = df.formatCellValue(cell);
        return v == null || v.isBlank() ? null : v.trim();
    }

    private String rongThanhNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private Integer nguyen(Row row, Integer c, DataFormatter df) {
        String s = chuoi(row, c, df);
        if (s == null) return null;
        Matcher m = SO_DAU.matcher(s);
        return m.find() ? Integer.valueOf(m.group(1)) : null;
    }

    /** "6 tháng" / "6" -> 6 ; trống -> null (gói mặc định). */
    private Integer soThangMua(String s) {
        if (s == null || s.isBlank()) return null;
        Matcher m = SO_DAU.matcher(s);
        return m.find() ? Integer.valueOf(m.group(1)) : null;
    }

    private LocalDate ngay(Row row, Integer c, DataFormatter df) {
        if (c == null || row == null) return null;
        Cell cell = row.getCell(c);
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC
                    || (cell.getCellType() == CellType.FORMULA
                        && cell.getCachedFormulaResultType() == CellType.NUMERIC)) {
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate();
                }
                double d = cell.getNumericCellValue();
                if (d > 20000) return DateUtil.getLocalDateTime(d).toLocalDate();
            }
        } catch (Exception ignore) {
            // rơi xuống parse chuỗi
        }
        String s = chuoi(row, c, df);
        if (s == null) return null;
        for (String pat : new String[]{"dd/MM/yyyy", "d/M/yyyy", "yyyy-MM-dd"}) {
            try {
                return LocalDate.parse(s, java.time.format.DateTimeFormatter.ofPattern(pat));
            } catch (Exception ignore) {
                // thử tiếp
            }
        }
        return null;
    }

    private void doiChieu(NhapExcelKetQua kq, int dongExcel, String ten, BaoHiemYTe bhyt,
                          Row row, Map<String, Integer> cot, DataFormatter df) {
        // Thành tiền
        Integer cThanhTien = cot.get("thanh tien");
        if (cThanhTien != null) {
            Cell cell = row.getCell(cThanhTien);
            if (cell != null) {
                Double sheetTien = soThucCell(cell);
                if (sheetTien != null && Math.abs(sheetTien - bhyt.getSoTienThu()) > 1) {
                    kq.getCanhBao().add(String.format(
                            "Dòng %d (%s): Thành tiền sheet=%,.0f nhưng tính lại=%,d",
                            dongExcel, ten, sheetTien, bhyt.getSoTienThu()));
                }
            }
        }
        // Hạn thẻ
        LocalDate sheetHan = ngay(row, cot.get("han the"), df);
        if (sheetHan != null && !sheetHan.equals(bhyt.getHanThe())) {
            kq.getCanhBao().add(String.format(
                    "Dòng %d (%s): Hạn thẻ sheet=%s nhưng tính lại=%s",
                    dongExcel, ten, sheetHan, bhyt.getHanThe()));
        }
    }

    private Double soThucCell(Cell cell) {
        try {
            if (cell.getCellType() == CellType.NUMERIC) return cell.getNumericCellValue();
            if (cell.getCellType() == CellType.FORMULA
                    && cell.getCachedFormulaResultType() == CellType.NUMERIC) {
                return cell.getNumericCellValue();
            }
        } catch (Exception ignore) {
            // bỏ qua
        }
        return null;
    }
}

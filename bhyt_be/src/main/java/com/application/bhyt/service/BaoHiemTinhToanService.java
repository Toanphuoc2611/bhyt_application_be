package com.application.bhyt.service;

import com.application.bhyt.config.BhytProperties;
import com.application.bhyt.entity.BaoHiemYTe;
import com.application.bhyt.entity.KhachHang;
import com.application.bhyt.enums.LoaiBHYT;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Nơi DUY NHẤT chứa hai công thức tài chính cốt lõi: hạn thẻ ({@code han_the}) và
 * số tiền thu ({@code so_tien_thu}). Mọi hằng số lấy từ {@link BhytProperties}
 * (file cấu hình ngoài) để đổi được mà không build lại.
 *
 * <p>Công thức bám theo đúng file Excel "DS Mua BHYT" của khách hàng.</p>
 *
 * <h3>1. Hạn thẻ</h3>
 * <b>Thẻ mới:</b>
 * <ul>
 *   <li>Gói mặc định (ô "Tháng mua" trống): {@code ngayMua + (soThangMacDinh + theMoiCongThemThang) tháng}
 *       — Excel: {@code EDATE(ngày mua, 13)}.</li>
 *   <li>Gói "N tháng": {@code ngayMua + N tháng + theMoiCongThemNgay ngày}
 *       — Excel: {@code EDATE(ngày mua, N) + 29}.</li>
 * </ul>
 * <b>Gia hạn:</b> tính từ {@code ngayHetHanCu} (cột "Ngày hết hạn" trong Excel):
 * <ul>
 *   <li>Nếu {@code (ngayMua - ngayHetHanCu) >= -giaHanNguongNgayMuaSom}:
 *       {@code ngayHetHanCu + (soThangMacDinh hoặc N) tháng}.</li>
 *   <li>Ngược lại (mua quá sớm): giữ nguyên {@code ngayHetHanCu}.</li>
 * </ul>
 *
 * <h3>2. Số tiền thu</h3>
 * <pre>
 * tyLe = coMstb ? tyLeMstb : tyLeTheoBac(soLanMuaCuaHo)
 *        (bậc 1..n theo danh sách ty-le-theo-bac; bậc vượt quá -> phần tử cuối)
 * soThang = (gói mặc định) ? soThangMacDinh : N
 * soTienThu = round( tyLe * mucDong * luongCoSo * soThang )
 * </pre>
 */
@Service
@RequiredArgsConstructor
public class BaoHiemTinhToanService {

    private final BhytProperties props;

    /**
     * Tính hạn thẻ.
     *
     * @param loai          "mới" hay "gia hạn"
     * @param ngayMua       ngày mua
     * @param ngayHetHanCu  hạn thẻ cũ (bắt buộc khi gia hạn; bỏ qua khi thẻ mới)
     * @param goiMacDinh    true nếu ô "Tháng mua" để trống
     * @param soThangMua    số tháng (đã quy đổi; nếu {@code goiMacDinh} thì bằng {@code soThangMacDinh})
     */
    public LocalDate tinhHanThe(String loai, LocalDate ngayMua, LocalDate ngayHetHanCu,
                                boolean goiMacDinh, int soThangMua) {
        boolean laGiaHan = LoaiBHYT.GIA_HAN.getGiaTri().equalsIgnoreCase(loai);

        if (laGiaHan) {
            if (ngayHetHanCu == null) {
                // Không có hạn cũ -> lùi về cách tính như thẻ mới để không bị null
                return tinhHanTheMoi(ngayMua, goiMacDinh, soThangMua);
            }
            long lech = ChronoUnit.DAYS.between(ngayHetHanCu, ngayMua); // ngayMua - ngayHetHanCu
            if (lech >= -props.getGiaHanNguongNgayMuaSom()) {
                return ngayHetHanCu.plusMonths(soThangMua);
            }
            return ngayHetHanCu;
        }

        return tinhHanTheMoi(ngayMua, goiMacDinh, soThangMua);
    }

    private LocalDate tinhHanTheMoi(LocalDate ngayMua, boolean goiMacDinh, int soThangMua) {
        if (goiMacDinh) {
            return ngayMua.plusMonths((long) soThangMua + props.getTheMoiCongThemThang());
        }
        return ngayMua.plusMonths(soThangMua).plusDays(props.getTheMoiCongThemNgay());
    }

    /**
     * Tính số tiền thu.
     *
     * @param coMstb        khách hàng có mstb hay không
     * @param soLanMuaCuaHo bậc "Người thứ" (không null)
     * @param soThangMua    số tháng đã quy đổi
     */
    public int tinhSoTienThu(boolean coMstb, int soLanMuaCuaHo, int soThangMua) {
        double tyLe = coMstb ? props.getTyLeMstb() : props.tyLeChoBac(soLanMuaCuaHo);
        double tong = tyLe * props.getMucDong() * props.getLuongCoSo() * soThangMua;
        return (int) Math.round(tong);
    }

    /** Số tháng thực tế dùng để tính, dựa trên gói. */
    public int soThangThucTe(boolean goiMacDinh, Integer soThangNhap) {
        return goiMacDinh || soThangNhap == null ? props.getSoThangMacDinh() : soThangNhap;
    }

    /** Áp lại cả {@code hanThe} và {@code soTienThu} lên entity từ trạng thái hiện tại. */
    public void apDungCongThuc(BaoHiemYTe bhyt) {
        boolean goiMacDinh = Boolean.TRUE.equals(bhyt.getGoiMacDinh());
        int soThang = soThangThucTe(goiMacDinh, bhyt.getSoThangMua());
        bhyt.setSoThangMua(soThang);

        KhachHang kh = bhyt.getKhachHang();
        boolean coMstb = kh != null && kh.getCoMstb() != null && kh.getCoMstb() == 1;

        bhyt.setHanThe(tinhHanThe(bhyt.getLoai(), bhyt.getNgayMua(), bhyt.getNgayHetHanCu(),
                goiMacDinh, soThang));
        bhyt.setSoTienThu(tinhSoTienThu(coMstb, bhyt.getSoLanMuaCuaHo(), soThang));
    }
}

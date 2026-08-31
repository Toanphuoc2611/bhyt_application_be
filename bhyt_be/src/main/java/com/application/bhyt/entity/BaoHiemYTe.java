package com.application.bhyt.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Thẻ bảo hiểm y tế / một lần mua BHYT của khách hàng (bảng {@code bao_hiem_y_te}).
 *
 * <p>Quy ước quan trọng:</p>
 * <ul>
 *   <li>{@code bhytMoiNhat = 1}: thẻ hiện hành; {@code = 0}: thẻ cũ đã bị thay bởi
 *       một lần gia hạn sau đó. Gia hạn KHÔNG xóa dòng cũ.</li>
 *   <li>{@code daXoa = 1}: xóa mềm.</li>
 *   <li>{@code hanThe}, {@code soTienThu}: cột tính toán - xem {@code BaoHiemTinhToanService}.</li>
 *   <li>{@code ngayHetHanCu}: khi gia hạn, đây là hạn của thẻ đang có (tương ứng cột
 *       "Ngày hết hạn" trong file Excel của khách hàng); là mốc để tính hạn thẻ mới.</li>
 *   <li>{@code goiMacDinh}: true = ô "Tháng mua" để trống (dùng {@code so_thang_mac_dinh}
 *       và cộng thêm 1 tháng cho thẻ mới); false = khách chọn "N tháng" cụ thể.</li>
 * </ul>
 */
@Entity
@Table(name = "bao_hiem_y_te", indexes = {
        @Index(name = "idx_bhyt_kh_moinhat", columnList = "id_khach_hang, bhyt_moi_nhat"),
        @Index(name = "idx_bhyt_han_the", columnList = "han_the")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaoHiemYTe implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang", nullable = false)
    private KhachHang khachHang;

    /** Ngày mua - bắt buộc. */
    @Column(name = "ngay_mua", nullable = false)
    private LocalDate ngayMua;

    /** Hạn của thẻ đang có (chỉ dùng khi {@code loai = "gia hạn"}). */
    @Column(name = "ngay_het_han_cu")
    private LocalDate ngayHetHanCu;

    /** Số tháng mua (đã quy đổi: gói mặc định lưu bằng {@code so_thang_mac_dinh}). */
    @Column(name = "so_thang_mua", nullable = false)
    private Integer soThangMua;

    /** true = ô "Tháng mua" để trống (gói mặc định). */
    @Column(name = "goi_mac_dinh")
    private Boolean goiMacDinh = Boolean.TRUE;

    /** Hạn thẻ (ngày hết hạn) - bắt buộc, tính tự động. */
    @Column(name = "han_the", nullable = false)
    private LocalDate hanThe;

    /**
     * Bậc "số lần mua của hộ" / "Người thứ" - do người dùng nhập ở FE, KHÔNG null.
     */
    @Column(name = "so_lan_mua_cua_ho", nullable = false)
    private Integer soLanMuaCuaHo;

    /** Số tiền thu (VNĐ) - tính tự động. */
    @Column(name = "so_tien_thu")
    private Integer soTienThu;

    @Column(name = "ngay_tao")
    private LocalDate ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDate ngayCapNhat;

    /** Đã nhận hoa hồng: 0 = chưa, 1 = rồi. */
    @Column(name = "da_nhan_hoa_hong")
    private Integer daNhanHoaHong = 0;

    /** 1 = thẻ hiện hành, 0 = thẻ cũ. */
    @Column(name = "bhyt_moi_nhat")
    private Integer bhytMoiNhat = 1;

    /** Cờ xóa mềm: 0 = còn, 1 = đã xóa. */
    @Column(name = "da_xoa")
    private Integer daXoa = 0;

    /** "mới" hoặc "gia hạn". */
    @Column(name = "loai")
    private String loai = "mới";

    /** Nơi đăng ký khám chữa bệnh ban đầu. */
    @Column(name = "noi_dang_ky")
    private String noiDangKy;

    @PrePersist
    protected void onCreate() {
        if (this.ngayTao == null) this.ngayTao = LocalDate.now();
        this.ngayCapNhat = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.ngayCapNhat = LocalDate.now();
    }
}

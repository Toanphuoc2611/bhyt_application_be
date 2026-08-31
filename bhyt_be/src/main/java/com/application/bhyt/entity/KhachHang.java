package com.application.bhyt.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Khách hàng mua BHYT (bảng {@code khach_hang}).
 *
 * <p>{@code daXoa = 1} là xóa mềm - mọi truy vấn danh sách/tìm kiếm/thống kê phải
 * lọc {@code da_xoa = 0}. Không bao giờ xóa vật lý.</p>
 */
@Entity
@Table(name = "khach_hang",
        uniqueConstraints = {@UniqueConstraint(name = "uk_khach_hang_cccd", columnNames = {"cccd"})},
        indexes = {@Index(name = "idx_khach_hang_ho_va_ten", columnList = "ho_va_ten")})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KhachHang implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Số CCCD - duy nhất, là trường tra cứu chính. */
    @Column(name = "cccd", unique = true)
    private String cccd;

    /** Họ và tên - bắt buộc. */
    @Column(name = "ho_va_ten", nullable = false)
    private String hoVaTen;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Column(name = "dia_chi")
    private String diaChi;

    @Column(name = "so_dien_thoai")
    private String soDienThoai;

    /** Liên lạc khác (Zalo, người thân...). */
    @Column(name = "lien_lac_khac")
    private String lienLacKhac;

    /** Đường dẫn file ảnh CCCD lưu cục bộ. */
    @Column(name = "hinh_anh")
    private String hinhAnh;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "ngay_tao")
    private LocalDate ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDate ngayCapNhat;

    /** BHYT khác mà khách hàng đã tham gia (nếu có). */
    @Column(name = "bhyt_khac")
    private String bhytKhac;

    /** Có mã số thẻ được miễn/giảm (mstb) hay không: 0 = không, 1 = có. */
    @Column(name = "co_mstb")
    private Integer coMstb = 0;

    /** Cờ xóa mềm: 0 = còn, 1 = đã xóa. */
    @Column(name = "da_xoa")
    private Integer daXoa = 0;

    /** Danh sách các thẻ BHYT của khách hàng (gồm cả thẻ cũ đã gia hạn). */
    @OneToMany(mappedBy = "khachHang", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BaoHiemYTe> baoHiemYTes = new ArrayList<>();

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

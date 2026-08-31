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
 * Hộ gia đình (bảng {@code ho_gia_dinh}).
 *
 * <p>{@code soThanhVien} là giá trị dẫn xuất: luôn bằng số dòng
 * {@code thanh_vien_ho_gia_dinh} đang hoạt động ({@code ngay_ket_thuc IS NULL}).
 * Không cho client tự set trực tiếp; tầng service cập nhật lại sau mỗi lần
 * thêm/bớt thành viên.</p>
 */
@Entity
@Table(name = "ho_gia_dinh")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HoGiaDinh implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Số thành viên hiện tại - do service tính lại, không nhận từ client. */
    @Column(name = "so_thanh_vien")
    private Integer soThanhVien = 0;

    /**
     * Tên/nhãn hộ (không bắt buộc). Khi nhập từ Excel, lấy từ cột "Hộ"
     * (thường là tên chủ hộ hoặc biệt danh).
     */
    @Column(name = "ten")
    private String ten;

    /** Đường dẫn ảnh sổ hộ khẩu / VNeID của hộ. */
    @Column(name = "hinh_anh")
    private String hinhAnh;

    @Column(name = "ngay_tao")
    private LocalDate ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDate ngayCapNhat;

    @OneToMany(mappedBy = "hoGiaDinh", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ThanhVienHoGiaDinh> thanhVien = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.ngayTao == null) this.ngayTao = LocalDate.now();
        if (this.soThanhVien == null) this.soThanhVien = 0;
        this.ngayCapNhat = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.ngayCapNhat = LocalDate.now();
    }
}

package com.application.bhyt.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Quan hệ thành viên - hộ gia đình (bảng {@code thanh_vien_ho_gia_dinh}).
 *
 * <p>Một người "rời" hộ bằng cách set {@code ngayKetThuc}, KHÔNG xóa dòng.
 * Hộ hiện tại của một khách hàng = dòng có {@code ngay_ket_thuc IS NULL}
 * (mỗi khách hàng tối đa 1 dòng như vậy - service phải bảo đảm điều này).</p>
 */
@Entity
@Table(name = "thanh_vien_ho_gia_dinh", indexes = {
        @Index(name = "idx_tvhgd_kh_ketthuc", columnList = "id_thanh_vien, ngay_ket_thuc")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThanhVienHoGiaDinh implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Khách hàng là thành viên (FK -> khach_hang.id). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_thanh_vien", nullable = false)
    private KhachHang khachHang;

    /** Hộ gia đình (FK -> ho_gia_dinh.id). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ho_gia_dinh", nullable = false)
    private HoGiaDinh hoGiaDinh;

    /** Ngày tham gia hộ. */
    @Column(name = "ngay_tao")
    private LocalDate ngayTao;

    /** Ngày rời hộ; NULL = đang là thành viên hiện tại. */
    @Column(name = "ngay_ket_thuc")
    private LocalDate ngayKetThuc;

    @PrePersist
    protected void onCreate() {
        if (this.ngayTao == null) this.ngayTao = LocalDate.now();
    }
}

package com.application.bhyt.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "thanh_vien_ho_gia_dinh")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThanhVienHoGiaDinh implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_thanh_vien", nullable = false)
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ho_gia_dinh", nullable = false)
    private HoGiaDinh hoGiaDinh;

    @Column(name = "ngay_tao")
    private LocalDate ngayTao;

    @Column(name = "ngay_ket_thuc")
    private LocalDate ngayKetThuc;

    @PrePersist
    protected void onCreate() {
        if (this.ngayTao == null) this.ngayTao = LocalDate.now();
    }
}

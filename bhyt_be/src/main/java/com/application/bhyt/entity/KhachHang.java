package com.application.bhyt.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "khach_hang", uniqueConstraints = {@UniqueConstraint(columnNames = {"cccd"})})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KhachHang implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "cccd", unique = true)
    private String cccd;

    @Column(name = "ho_va_ten", nullable = false)
    private String hoVaTen;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Column(name = "dia_chi")
    private String diaChi;

    @Column(name = "so_dien_thoai")
    private String soDienThoai;

    @Column(name = "lien_lac_khac")
    private String lienLacKhac;

    @Column(name = "hinh_anh")
    private String hinhAnh;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "ngay_tao")
    private LocalDate ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDate ngayCapNhat;

    @Column(name = "bhyt_khac")
    private String bhytKhac;

    @Column(name = "co_mstb")
    private Integer coMstb = 0;

    @Column(name = "da_xoa")
    private Integer daXoa = 0;

    // One customer can have multiple BHYT records
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

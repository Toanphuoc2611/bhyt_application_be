package com.application.bhyt.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "so_thanh_vien")
    private Integer soThanhVien = 0;

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
        if (this.thanhVien != null) this.soThanhVien = this.thanhVien.size();
    }
}

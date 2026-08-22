package com.application.bhyt.entity;

import com.application.bhyt.enums.LoaiBHYT;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "bao_hiem_y_te")
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

    @Column(name = "ngay_mua", nullable = false)
    private LocalDate ngayMua;

    @Column(name = "ngay_co_han")
    private LocalDate ngayCoHan;

    @Column(name = "so_thang_mua", nullable = false)
    private Integer soThangMua;

    @Column(name = "han_the", nullable = false)
    private LocalDate hanThe;

    @Column(name = "so_lan_mua_cua_ho")
    private Integer soLanMuaCuaHo = 1;

    @Column(name = "so_tien_thu")
    private Integer soTienThu;

    @Column(name = "ngay_tao")
    private LocalDate ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDate ngayCapNhat;

    @Column(name = "da_nhan_hoa_hong")
    private Integer daNhanHoaHong = 0;

    @Column(name = "bhyt_moi_nhat")
    private Integer bhytMoiNhat = 1;

    @Column(name = "da_xoa")
    private Integer daXoa = 0;

    @Column(name = "loai")
    private String loai = LoaiBHYT.MOI.getLoai();

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

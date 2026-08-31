package com.application.bhyt.repository;

import com.application.bhyt.entity.ThanhVienHoGiaDinh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Truy vấn quan hệ thành viên - hộ gia đình.
 * "Đang hoạt động" nghĩa là {@code ngay_ket_thuc IS NULL}.
 */
@Repository
public interface ThanhVienHoGiaDinhRepository extends JpaRepository<ThanhVienHoGiaDinh, Integer> {

    /** Các thành viên đang hoạt động của một hộ. */
    @Query("SELECT t FROM ThanhVienHoGiaDinh t WHERE t.hoGiaDinh.id = :idHo AND t.ngayKetThuc IS NULL ORDER BY t.id")
    List<ThanhVienHoGiaDinh> findThanhVienDangHoatDong(@Param("idHo") Integer idHo);

    /** Dòng thành viên đang hoạt động của một khách hàng (hộ hiện tại của họ). */
    @Query("SELECT t FROM ThanhVienHoGiaDinh t WHERE t.khachHang.id = :idKhachHang AND t.ngayKetThuc IS NULL")
    Optional<ThanhVienHoGiaDinh> findHoHienTaiCuaKhachHang(@Param("idKhachHang") Integer idKhachHang);

    @Query("SELECT COUNT(t) FROM ThanhVienHoGiaDinh t WHERE t.hoGiaDinh.id = :idHo AND t.ngayKetThuc IS NULL")
    int demThanhVienDangHoatDong(@Param("idHo") Integer idHo);
}

package com.application.bhyt.repository;

import com.application.bhyt.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {

    @Query("""
        select count(k) from KhachHang k
        where k.daXoa = 0
    """)
    Integer countSoKhachHang();

    @Query("""
        select count(k) from KhachHang k
        where k.daXoa = 0
          and k.id not in (
              select tv.khachHang.id from ThanhVienHoGiaDinh tv where tv.ngayKetThuc is null
          )
    """)
    Integer countKhachHangChuaCoHo();
}

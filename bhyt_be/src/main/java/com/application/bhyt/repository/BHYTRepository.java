package com.application.bhyt.repository;

import com.application.bhyt.entity.BaoHiemYTe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;

public interface BHYTRepository extends JpaRepository<BaoHiemYTe, Integer> {
    @Query("""
        Select count(bhyt) from BaoHiemYTe bhyt
        where bhyt.daXoa = 0 and
              bhyt.muaMoiNhat = 1 and
              bhyt.hanThe >= :from and
              bhyt.hanThe <= :to
    """)
    Integer thongKeBHYTTheoHanThe(LocalDate from, LocalDate to);

    @Query("""
        select count(b) from BaoHiemYTe b
        where b.daXoa = 0 and b.daNhanHoaHong = 0 and b.muaMoiNhat = 1
    """)
    Integer countChuaNhanHoaHong();

    @Query("""
        select sum(b.soTienThu) from BaoHiemYTe b
        where b.daXoa = 0 and b.daNhanHoaHong = 1
          and b.ngayTao >= :from
          and b.ngayTao <= :to
    """)
    Integer sumSoTienThuByNgayTaoBetween(LocalDate from, LocalDate to);

    @Query("""
        select count(b) from BaoHiemYTe b
        where b.daXoa = 0
          and b.loai = :loai
          and b.ngayTao >= :from
          and b.ngayTao <= :to
    """)
    Integer countByLoaiAndNgayTaoBetween(String loai, LocalDate from, LocalDate to);
}

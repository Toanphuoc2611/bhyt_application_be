package com.application.bhyt.repository;

import com.application.bhyt.entity.BaoHiemYTe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Truy vấn thẻ BHYT. Tất cả đều lọc {@code da_xoa = 0}.
 */
@Repository
public interface BaoHiemYTeRepository extends JpaRepository<BaoHiemYTe, Integer> {

    @Query("SELECT b FROM BaoHiemYTe b WHERE b.id = :id AND b.daXoa = 0")
    Optional<BaoHiemYTe> findActiveById(@Param("id") Integer id);

    /** Thẻ hiện hành của khách hàng (bhyt_moi_nhat = 1). */
    @Query("SELECT b FROM BaoHiemYTe b WHERE b.daXoa = 0 AND b.bhytMoiNhat = 1 AND b.khachHang.id = :idKhachHang")
    Optional<BaoHiemYTe> findTheHienHanh(@Param("idKhachHang") Integer idKhachHang);

    /** Toàn bộ lịch sử mua của khách hàng, mới nhất trước. */
    @Query("SELECT b FROM BaoHiemYTe b WHERE b.daXoa = 0 AND b.khachHang.id = :idKhachHang ORDER BY b.ngayMua DESC, b.id DESC")
    List<BaoHiemYTe> findLichSu(@Param("idKhachHang") Integer idKhachHang);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM BaoHiemYTe b WHERE b.daXoa = 0 AND b.khachHang.id = :idKhachHang")
    boolean khachHangDaCoBhyt(@Param("idKhachHang") Integer idKhachHang);

    // ---------------------------------------------------------------------
    // Danh sách Trang 2: tìm kiếm theo tên/CCCD khách hàng + lọc trạng thái
    // ---------------------------------------------------------------------

    String DK_SEARCH = """
            ( :search = ''
              OR LOWER(b.khachHang.hoVaTen) LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(COALESCE(b.khachHang.cccd, '')) LIKE LOWER(CONCAT('%', :search, '%')) )
            """;

    @Query("SELECT b FROM BaoHiemYTe b WHERE b.daXoa = 0 AND " + DK_SEARCH)
    Page<BaoHiemYTe> locTatCa(@Param("search") String search, Pageable pageable);

    @Query("SELECT b FROM BaoHiemYTe b WHERE b.daXoa = 0 AND b.hanThe < :homNay AND " + DK_SEARCH)
    Page<BaoHiemYTe> locHetHan(@Param("search") String search, @Param("homNay") LocalDate homNay, Pageable pageable);

    @Query("SELECT b FROM BaoHiemYTe b WHERE b.daXoa = 0 AND b.hanThe >= :homNay AND b.hanThe <= :bienNgay AND " + DK_SEARCH)
    Page<BaoHiemYTe> locGanHetHan(@Param("search") String search,
                                 @Param("homNay") LocalDate homNay,
                                 @Param("bienNgay") LocalDate bienNgay,
                                 Pageable pageable);

    @Query("SELECT b FROM BaoHiemYTe b WHERE b.daXoa = 0 AND b.daNhanHoaHong = 0 AND " + DK_SEARCH)
    Page<BaoHiemYTe> locChuaNhanHoaHong(@Param("search") String search, Pageable pageable);

    // ---------------------------------------------------------------------
    // Thống kê Trang 1
    // ---------------------------------------------------------------------

    @Query("SELECT COUNT(b) FROM BaoHiemYTe b WHERE b.daXoa = 0 AND b.hanThe >= :homNay AND b.hanThe <= :bienNgay")
    long demSapHetHan(@Param("homNay") LocalDate homNay, @Param("bienNgay") LocalDate bienNgay);

    @Query("SELECT COUNT(b) FROM BaoHiemYTe b WHERE b.daXoa = 0 AND b.bhytMoiNhat = 1 AND b.daNhanHoaHong = 0")
    long demChuaNhanHoaHongHienHanh();

    @Query("SELECT COALESCE(SUM(b.soTienThu), 0) FROM BaoHiemYTe b WHERE b.daXoa = 0 AND b.ngayMua >= :tuNgay AND b.ngayMua <= :denNgay")
    long tongTienTheoNgayMua(@Param("tuNgay") LocalDate tuNgay, @Param("denNgay") LocalDate denNgay);

    @Query("SELECT COUNT(b) FROM BaoHiemYTe b WHERE b.daXoa = 0 AND b.ngayMua >= :tuNgay AND b.ngayMua <= :denNgay")
    long demTheTheoNgayMua(@Param("tuNgay") LocalDate tuNgay, @Param("denNgay") LocalDate denNgay);

    @Query("SELECT COUNT(b) FROM BaoHiemYTe b WHERE b.daXoa = 0 AND b.loai = :loai AND b.ngayMua >= :tuNgay AND b.ngayMua <= :denNgay")
    long demTheTheoLoaiVaNgayMua(@Param("loai") String loai,
                                 @Param("tuNgay") LocalDate tuNgay,
                                 @Param("denNgay") LocalDate denNgay);

    // ---------------------------------------------------------------------
    // Trang 4: toàn bộ thẻ còn hiệu lực để FE dựng file Excel
    // ---------------------------------------------------------------------

    @Query("SELECT b FROM BaoHiemYTe b WHERE b.daXoa = 0 ORDER BY b.khachHang.id, b.id")
    List<BaoHiemYTe> findChoXuatFile();
}

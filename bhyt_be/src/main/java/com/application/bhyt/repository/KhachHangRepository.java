package com.application.bhyt.repository;

import com.application.bhyt.entity.KhachHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Truy vấn khách hàng. Mọi truy vấn danh sách đều lọc {@code da_xoa = 0}
 * (xóa mềm) - việc lọc được viết tường minh trong từng câu query để nhìn thấy rõ.
 */
@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {

    Optional<KhachHang> findByCccd(String cccd);

    /** Lấy khách hàng còn hiệu lực theo id. */
    @Query("SELECT k FROM KhachHang k WHERE k.id = :id AND k.daXoa = 0")
    Optional<KhachHang> findActiveById(@Param("id") Integer id);

    @Query("SELECT k FROM KhachHang k WHERE k.daXoa = 0 ORDER BY k.id")
    List<KhachHang> findAllActive();

    // ---------------------------------------------------------------------
    // Danh sách Trang 3: tìm kiếm theo tên/CCCD + bộ lọc
    // search = "" nghĩa là không lọc theo từ khóa.
    // ---------------------------------------------------------------------

    /** Bộ lọc TAT_CA. */
    @Query("""
            SELECT k FROM KhachHang k
            WHERE k.daXoa = 0
              AND ( :search = ''
                    OR LOWER(k.hoVaTen) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(COALESCE(k.cccd, '')) LIKE LOWER(CONCAT('%', :search, '%')) )
            """)
    Page<KhachHang> timKiem(@Param("search") String search, Pageable pageable);

    /** Bộ lọc KHONG_CO_HO: không có dòng thành viên hộ đang hoạt động. */
    @Query("""
            SELECT k FROM KhachHang k
            WHERE k.daXoa = 0
              AND ( :search = ''
                    OR LOWER(k.hoVaTen) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(COALESCE(k.cccd, '')) LIKE LOWER(CONCAT('%', :search, '%')) )
              AND NOT EXISTS (
                    SELECT 1 FROM ThanhVienHoGiaDinh t
                    WHERE t.khachHang = k AND t.ngayKetThuc IS NULL )
            """)
    Page<KhachHang> timKiemKhongCoHo(@Param("search") String search, Pageable pageable);

    /** Bộ lọc CHUA_MUA_BHYT: chưa có thẻ BHYT nào (chưa xóa). */
    @Query("""
            SELECT k FROM KhachHang k
            WHERE k.daXoa = 0
              AND ( :search = ''
                    OR LOWER(k.hoVaTen) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(COALESCE(k.cccd, '')) LIKE LOWER(CONCAT('%', :search, '%')) )
              AND NOT EXISTS (
                    SELECT 1 FROM BaoHiemYTe b
                    WHERE b.khachHang = k AND b.daXoa = 0 )
            """)
    Page<KhachHang> timKiemChuaMuaBhyt(@Param("search") String search, Pageable pageable);

    @Query("SELECT COUNT(k) FROM KhachHang k WHERE k.daXoa = 0")
    long demConHieuLuc();

    /** Số khách hàng chưa thuộc hộ nào (một câu query, không lặp vòng). */
    @Query("""
            SELECT COUNT(k) FROM KhachHang k
            WHERE k.daXoa = 0
              AND NOT EXISTS (
                    SELECT 1 FROM ThanhVienHoGiaDinh t
                    WHERE t.khachHang = k AND t.ngayKetThuc IS NULL )
            """)
    long demKhachHangChuaCoHo();
}

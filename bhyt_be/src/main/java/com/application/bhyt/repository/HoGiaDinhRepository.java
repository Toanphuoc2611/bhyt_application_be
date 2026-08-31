package com.application.bhyt.repository;

import com.application.bhyt.entity.HoGiaDinh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Truy vấn hộ gia đình. Hộ không có cờ xóa mềm nên dùng trực tiếp
 * {@code findAll(Pageable)} và {@code count()} của {@link JpaRepository}.
 */
@Repository
public interface HoGiaDinhRepository extends JpaRepository<HoGiaDinh, Integer> {
}

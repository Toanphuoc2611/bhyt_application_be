package com.application.bhyt.repository;

import com.application.bhyt.entity.HoGiaDinh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface HoGiaDinhRepository extends JpaRepository<HoGiaDinh, Integer> {

    @Query("""
        select count(h) from HoGiaDinh h
            where h.soThanhVien > 0
    """)
    Integer countHoGiaDinh();
}

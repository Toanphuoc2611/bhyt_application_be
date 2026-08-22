package com.application.bhyt.service;

import com.application.bhyt.repository.HoGiaDinhRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HoGiaDinhService {
    HoGiaDinhRepository repository;

    /**
     * Đếm số lượng hộ gia đình
     * @return
     */
    public Integer countHoGiaDinh() {
        return repository.countHoGiaDinh();
    }
}

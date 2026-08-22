package com.application.bhyt.service;

import com.application.bhyt.repository.ThanhVienHGDRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ThanhVienHGDService {
    ThanhVienHGDRepository repository;
}

package com.application.bhyt.service;

import com.application.bhyt.repository.KhachHangRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class KhachHangService {
    KhachHangRepository repository ;

    /**
     *  Đếm số lượng khách hàng
     * @return
     */
    public Integer countSoKhachHang() {
        Integer result = repository.countSoKhachHang();
        return result == null ? 0 : result;
    }

    /**
     *  Đếm số lượng khách hàng chưa có hộ gia đình
     * @return
     */
    public Integer countKhachHangChuaCoHo() {
        Integer result = repository.countKhachHangChuaCoHo();
        return result == null ? 0 : result;
    }
}

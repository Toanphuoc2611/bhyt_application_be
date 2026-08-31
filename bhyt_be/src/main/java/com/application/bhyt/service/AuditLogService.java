package com.application.bhyt.service;

import com.application.bhyt.enums.ActionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Ghi nhật ký thao tác nghiệp vụ.
 *
 * <p>Client chưa yêu cầu lưu audit vào DB nên hiện chỉ ghi ra file log riêng
 * (logger tên {@code operation} - xem {@code logback-spring.xml}). Có thể nâng cấp
 * ghi xuống bảng sau này mà không đổi chỗ gọi.</p>
 */
@Service
public class AuditLogService {

    private static final Logger LOG_THAO_TAC = LoggerFactory.getLogger("operation");

    public void ghiLai(ActionType hanhDong, String bang, Integer idBanGhi, String moTa) {
        LOG_THAO_TAC.info("[{}] {} | bảng={} | id={} | {}",
                LocalDateTime.now(), hanhDong, bang, idBanGhi, moTa);
    }
}

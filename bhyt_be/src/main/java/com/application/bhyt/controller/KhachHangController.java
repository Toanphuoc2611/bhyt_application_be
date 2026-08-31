package com.application.bhyt.controller;

import com.application.bhyt.dto.request.CreateKhachHangRequest;
import com.application.bhyt.dto.request.UpdateKhachHangRequest;
import com.application.bhyt.dto.response.ApiResponse;
import com.application.bhyt.dto.response.KhachHangChiTietDto;
import com.application.bhyt.dto.response.KhachHangDto;
import com.application.bhyt.dto.response.PageResponse;
import com.application.bhyt.service.KhachHangService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * API quản lý khách hàng - Trang 3. Base path {@code /api/v1/khach-hang}.
 * Mọi endpoint đều ngầm định {@code da_xoa = 0}.
 */
@RestController
@RequestMapping("/api/v1/khach-hang")
@RequiredArgsConstructor
public class KhachHangController {

    private final KhachHangService khachHangService;

    /** Danh sách + tìm kiếm (tên/CCCD) + lọc: TAT_CA | KHONG_CO_HO | CHUA_MUA_BHYT. */
    @GetMapping
    public ApiResponse<PageResponse<KhachHangDto>> danhSach(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ApiResponse.thanhCong(khachHangService.danhSach(search, filter, pageable));
    }

    /** Thông tin một khách hàng. */
    @GetMapping("/{id}")
    public ApiResponse<KhachHangDto> layTheoId(@PathVariable Integer id) {
        return ApiResponse.thanhCong(khachHangService.layTheoId(id));
    }

    /** Chi tiết: khách hàng + hộ hiện tại + thành viên cùng hộ + lịch sử mua BHYT. */
    @GetMapping("/{id}/chi-tiet")
    public ApiResponse<KhachHangChiTietDto> chiTiet(@PathVariable Integer id) {
        return ApiResponse.thanhCong(khachHangService.chiTiet(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<KhachHangDto>> taoMoi(@Valid @RequestBody CreateKhachHangRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.thanhCong(khachHangService.taoMoi(req)));
    }

    @PutMapping("/{id}")
    public ApiResponse<KhachHangDto> capNhat(@PathVariable Integer id,
                                             @Valid @RequestBody UpdateKhachHangRequest req) {
        return ApiResponse.thanhCong(khachHangService.capNhat(id, req));
    }

    /** Xóa mềm (da_xoa = 1). */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> xoa(@PathVariable Integer id) {
        khachHangService.xoaMem(id);
        return ApiResponse.thanhCong();
    }
}

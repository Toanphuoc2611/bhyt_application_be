package com.application.bhyt.controller;

import com.application.bhyt.dto.request.LuuHoGiaDinhRequest;
import com.application.bhyt.dto.request.ThemThanhVienRequest;
import com.application.bhyt.dto.response.ApiResponse;
import com.application.bhyt.dto.response.HoGiaDinhChiTietDto;
import com.application.bhyt.dto.response.HoGiaDinhDto;
import com.application.bhyt.dto.response.PageResponse;
import com.application.bhyt.service.HoGiaDinhService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * API quản lý hộ gia đình - Trang 3. Base path {@code /api/v1/ho-gia-dinh}.
 */
@RestController
@RequestMapping("/api/v1/ho-gia-dinh")
@RequiredArgsConstructor
public class HoGiaDinhController {

    private final HoGiaDinhService hoGiaDinhService;

    @GetMapping
    public ApiResponse<PageResponse<HoGiaDinhDto>> danhSach(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ApiResponse.thanhCong(hoGiaDinhService.danhSach(pageable));
    }

    /** Chi tiết hộ kèm danh sách thành viên hiện tại. */
    @GetMapping("/{id}")
    public ApiResponse<HoGiaDinhChiTietDto> chiTiet(@PathVariable Integer id) {
        return ApiResponse.thanhCong(hoGiaDinhService.chiTiet(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<HoGiaDinhDto>> taoMoi(@RequestBody(required = false) LuuHoGiaDinhRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.thanhCong(hoGiaDinhService.taoMoi(req)));
    }

    @PutMapping("/{id}")
    public ApiResponse<HoGiaDinhDto> capNhat(@PathVariable Integer id, @RequestBody LuuHoGiaDinhRequest req) {
        return ApiResponse.thanhCong(hoGiaDinhService.capNhat(id, req));
    }

    /** Thêm một khách hàng vào hộ: body { idKhachHang }. */
    @PostMapping("/{id}/thanh-vien")
    public ResponseEntity<ApiResponse<Void>> themThanhVien(@PathVariable Integer id,
                                                          @Valid @RequestBody ThemThanhVienRequest req) {
        hoGiaDinhService.themThanhVien(id, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.thanhCong());
    }

    /** Thành viên rời hộ: set ngay_ket_thuc, không xóa dòng. */
    @DeleteMapping("/{id}/thanh-vien/{idKhachHang}")
    public ApiResponse<Void> xoaThanhVien(@PathVariable Integer id, @PathVariable Integer idKhachHang) {
        hoGiaDinhService.xoaThanhVien(id, idKhachHang);
        return ApiResponse.thanhCong();
    }
}

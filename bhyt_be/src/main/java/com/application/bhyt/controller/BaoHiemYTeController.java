package com.application.bhyt.controller;

import com.application.bhyt.dto.request.CreateBaoHiemYTeRequest;
import com.application.bhyt.dto.request.GiaHanBaoHiemYTeRequest;
import com.application.bhyt.dto.request.UpdateBaoHiemYTeRequest;
import com.application.bhyt.dto.request.XacNhanHoaHongRequest;
import com.application.bhyt.dto.response.ApiResponse;
import com.application.bhyt.dto.response.BaoHiemYTeDto;
import com.application.bhyt.dto.response.PageResponse;
import com.application.bhyt.service.BaoHiemYTeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * API quản lý thẻ BHYT - Trang 2. Base path {@code /api/v1/bao-hiem-y-te}.
 */
@RestController
@RequestMapping("/api/v1/bao-hiem-y-te")
@RequiredArgsConstructor
public class BaoHiemYTeController {

    private final BaoHiemYTeService baoHiemYTeService;

    /**
     * Danh sách + tìm kiếm (tên/CCCD khách hàng) + lọc trạng thái:
     * HET_HAN | GAN_HET_HAN_7 | GAN_HET_HAN_14 | GAN_HET_HAN_30 | GAN_HET_HAN_60 | CHUA_NHAN_HOA_HONG.
     */
    @GetMapping
    public ApiResponse<PageResponse<BaoHiemYTeDto>> danhSach(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String trangThai,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ApiResponse.thanhCong(baoHiemYTeService.danhSach(search, trangThai, pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<BaoHiemYTeDto> layTheoId(@PathVariable Integer id) {
        return ApiResponse.thanhCong(baoHiemYTeService.layTheoId(id));
    }

    /** Tạo thẻ mới (loai = "mới"). Server tự tính hanThe, soTienThu. */
    @PostMapping
    public ResponseEntity<ApiResponse<BaoHiemYTeDto>> taoMoi(@Valid @RequestBody CreateBaoHiemYTeRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.thanhCong(baoHiemYTeService.taoMoi(req)));
    }

    /** Gia hạn thẻ: tạo dòng "gia hạn" mới, giữ nguyên bậc, lật thẻ cũ. */
    @PostMapping("/{id}/gia-han")
    public ResponseEntity<ApiResponse<BaoHiemYTeDto>> giaHan(@PathVariable Integer id,
                                                            @Valid @RequestBody GiaHanBaoHiemYTeRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.thanhCong(baoHiemYTeService.giaHan(id, req)));
    }

    /** Cập nhật thẻ (ví dụ điền ngayCoHan -> server tính lại hanThe). */
    @PutMapping("/{id}")
    public ApiResponse<BaoHiemYTeDto> capNhat(@PathVariable Integer id,
                                              @Valid @RequestBody UpdateBaoHiemYTeRequest req) {
        return ApiResponse.thanhCong(baoHiemYTeService.capNhat(id, req));
    }

    /** Xóa mềm (da_xoa = 1). */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> xoa(@PathVariable Integer id) {
        baoHiemYTeService.xoaMem(id);
        return ApiResponse.thanhCong();
    }

    /** Xác nhận đã nhận hoa hồng hàng loạt: body { ids: [...] }. */
    @PatchMapping("/xac-nhan-hoa-hong")
    public ApiResponse<Void> xacNhanHoaHong(@Valid @RequestBody XacNhanHoaHongRequest req) {
        baoHiemYTeService.xacNhanHoaHong(req.getIds());
        return ApiResponse.thanhCong();
    }
}

package com.application.bhyt.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/** Xác nhận đã nhận hoa hồng hàng loạt: danh sách ID thẻ BHYT. */
@Data
public class XacNhanHoaHongRequest {

    @NotEmpty(message = "Danh sách ID không được rỗng")
    private List<Integer> ids;
}

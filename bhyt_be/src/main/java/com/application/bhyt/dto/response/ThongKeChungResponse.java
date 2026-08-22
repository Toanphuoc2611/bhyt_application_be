package com.application.bhyt.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ThongKeChungResponse {
    int bhytHetHan;
    int tongSoHo;
    int tongBhytChuaNhanHoaHong;
    int tongSoKhachHangChuaCoHo;
    int tongSoKhachHang;
}

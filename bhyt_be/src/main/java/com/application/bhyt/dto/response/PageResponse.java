package com.application.bhyt.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Kết quả phân trang trả cho FE theo quy ước chung của dự án:
 * <pre>{ content, totalElements, totalPages, page, size }</pre>
 * ({@code page} đánh số từ 0).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    /** Danh sách bản ghi của trang hiện tại. */
    private List<T> content;

    /** Tổng số bản ghi (tất cả các trang). */
    private long totalElements;

    /** Tổng số trang. */
    private int totalPages;

    /** Chỉ số trang hiện tại (bắt đầu từ 0). */
    private int page;

    /** Số bản ghi mỗi trang. */
    private int size;

    /** Bọc một {@link Page} của Spring Data, đồng thời map entity -> DTO. */
    public static <E, D> PageResponse<D> from(Page<E> page, Function<E, D> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize()
        );
    }
}

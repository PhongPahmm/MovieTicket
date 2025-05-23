package com.example.movieticket.util;

import com.example.movieticket.dto.response.PageResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public class PaginationUtil {

    public static <T, U> PageResponse<U> mapToPageResponse(Page<T> page, Function<T, U> mapper) {
        List<U> items = page.getContent().stream()
                .map(mapper)
                .toList();

        return PageResponse.<U>builder()
                .items(items)
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isLast(page.isLast())
                .build();
    }
}


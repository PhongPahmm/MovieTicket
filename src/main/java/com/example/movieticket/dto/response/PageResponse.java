package com.example.movieticket.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class PageResponse<T> {
    private final List<T> items;
    private final int currentPage;
    private final int pageSize;
    private final long totalItems;
    private final int totalPages;
    private final boolean isLast;
}

package com.example.movieticket.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class PageResponse<T> {
    private final List<T> items;
    private final int currentPage;
    private final int pageSize;
    private final long totalItems;
    private final int totalPages;
}

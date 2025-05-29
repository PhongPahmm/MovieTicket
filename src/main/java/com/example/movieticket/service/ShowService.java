package com.example.movieticket.service;

import com.example.movieticket.dto.request.ShowRequest;
import com.example.movieticket.dto.response.PageResponse;
import com.example.movieticket.dto.response.ShowResponse;

public interface ShowService {
    ShowResponse createShow(ShowRequest request);
    ShowResponse updateShow(Integer showId, ShowRequest request);
    ShowResponse changeActive(Integer showId, Boolean active);
    PageResponse<ShowResponse> getShowByMovie(Integer movieId, int page, int size);
    PageResponse<ShowResponse> getAllShow(int page, int size);
    ShowResponse getShowById(Integer showId);
}

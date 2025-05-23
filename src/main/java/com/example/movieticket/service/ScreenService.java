package com.example.movieticket.service;

import com.example.movieticket.dto.request.ScreenRequest;
import com.example.movieticket.dto.response.ScreenResponse;

import java.util.List;

public interface ScreenService {
    ScreenResponse createScreen(ScreenRequest request);
    ScreenResponse updateScreen(Integer screenId, ScreenRequest request);
    ScreenResponse getScreeById(Integer id);
    List<ScreenResponse> getAllScreens();
    ScreenResponse changeScreenStatus(Integer id, Boolean isActive);
}

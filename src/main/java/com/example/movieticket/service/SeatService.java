package com.example.movieticket.service;

import com.example.movieticket.dto.request.SeatRequest;
import com.example.movieticket.dto.response.SeatResponse;

import java.util.List;

public interface SeatService {
    SeatResponse createSeat(SeatRequest request);
    SeatResponse updateSeat(Integer seatId, SeatRequest request);
    List<SeatResponse> getSeatsByScreen(Integer screenId);
    List<SeatResponse> getSeatsByShow(Integer showId);
    Void deleteSeat(Integer seatId);
}

package com.example.movieticket.service;

import com.example.movieticket.common.SeatType;
import com.example.movieticket.dto.request.PriceRequest;
import com.example.movieticket.dto.response.PageResponse;
import com.example.movieticket.dto.response.PriceResponse;

import java.time.LocalDate;
import java.util.List;

public interface PriceService {
    PriceResponse createPrice(PriceRequest priceRequest);
    PriceResponse updatePrice(Integer priceId, PriceRequest priceRequest);
    Void deletePrice(Integer priceId);
    List<PriceResponse> getPricesByShow(Integer showId);
    PriceResponse getValidPrice(Integer showId, SeatType seatType, LocalDate date);
    PriceResponse getPriceByShowAndSeat(Integer showId, Integer seatId);
    PageResponse<PriceResponse> getAllPrices(int page, int size);
    Integer getAmountByScreenAndSeat(Integer screenId, SeatType seatType);
    com.example.movieticket.dto.response.ScreenPriceResponse createOrUpdateScreenPrice(com.example.movieticket.dto.request.ScreenPriceRequest request);
    java.util.List<com.example.movieticket.dto.response.ScreenPriceResponse> getScreenPricesByScreen(Integer screenId);
}

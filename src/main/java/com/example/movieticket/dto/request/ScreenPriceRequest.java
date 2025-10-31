package com.example.movieticket.dto.request;

import com.example.movieticket.common.SeatType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScreenPriceRequest {
    private Integer screenId;
    private SeatType seatType;
    private Integer amount;
}



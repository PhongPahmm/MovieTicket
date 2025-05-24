package com.example.movieticket.dto.request;

import com.example.movieticket.common.SeatType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PriceRequest {
    private Integer showId;
    private Integer amount;
    private SeatType seatType;
    private LocalDate validFrom;
    private LocalDate validTo;
}

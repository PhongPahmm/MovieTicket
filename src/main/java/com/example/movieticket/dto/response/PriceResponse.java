package com.example.movieticket.dto.response;

import com.example.movieticket.common.SeatType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class PriceResponse {
    private Integer priceId;
    private Integer showId;
    private Integer amount;
    private LocalDate validFrom;
    private LocalDate validTo;
    private SeatType seatType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

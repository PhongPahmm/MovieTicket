package com.example.movieticket.dto.response;

import com.example.movieticket.common.SeatType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SeatResponse {
    private Integer id;
    private Integer screenId;
    private Integer row;
    private Integer number;
    private SeatType seatType;
    private Boolean active;
    private boolean booked;
}

package com.example.movieticket.dto.request;

import com.example.movieticket.common.SeatType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SeatRequest {
    private Integer screenId;
    private Integer row;
    private Integer number;
    private SeatType seatType;
    private Boolean active;
}

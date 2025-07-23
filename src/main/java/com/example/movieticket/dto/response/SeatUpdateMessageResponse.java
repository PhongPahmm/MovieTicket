package com.example.movieticket.dto.response;

import com.example.movieticket.common.SeatStatus;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SeatUpdateMessageResponse {
    private List<Integer> seatIds;
    private SeatStatus seatStatus;
}

package com.example.movieticket.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Builder
public class ShowResponse {
    Integer showId;
    Integer movieId;
    Integer screenId;
    LocalDate showDate;
    LocalTime startTime;
    LocalTime endTime;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    Boolean active;
}

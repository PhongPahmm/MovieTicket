package com.example.movieticket.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class ShowRequest {
    Integer movieId;
    Integer screenId;
    LocalDate showDate;
    LocalTime startTime;
    LocalTime endTime;
    Boolean active;
}

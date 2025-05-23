package com.example.movieticket.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ScreenResponse {
    private Integer id;
    private String name;
    private Integer totalSeats;
    private Boolean is3D;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

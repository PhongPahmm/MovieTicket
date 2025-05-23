package com.example.movieticket.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScreenRequest {
    private String name;
    private Integer totalSeats;
    private Boolean is3D;
    private Boolean active;
}

package com.example.movieticket.dto.request;

import lombok.*;

@Getter
@Setter
public class ReviewRequest {
    private int movieId;
    private Integer rating;
    private String comment;
}

package com.example.movieticket.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
public class MovieReviewResponse {
    private double averageRating;
    private List<ReviewResponse> reviews;
}

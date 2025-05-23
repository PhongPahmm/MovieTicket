package com.example.movieticket.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
public class MovieReviewResponse {
    private double averageRating;
    private PageResponse<ReviewResponse> reviews;
}

package com.example.movieticket.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ReviewResponse {
    int reviewId;
    int userId;
    int movieId;
    int rating;
    String comment;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}

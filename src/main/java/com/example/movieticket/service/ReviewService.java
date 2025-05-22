package com.example.movieticket.service;

import com.example.movieticket.dto.request.ReviewRequest;
import com.example.movieticket.dto.response.MovieReviewResponse;
import com.example.movieticket.dto.response.ReviewResponse;

import java.util.List;

public interface ReviewService {
    ReviewResponse createReview(int userId, ReviewRequest request);
    ReviewResponse updateReview(int reviewId, ReviewRequest request);
    List<ReviewResponse> getAllReviews();
    Void deleteReview(int reviewId);
    MovieReviewResponse getReviewByMovieId(Integer movieId);
}

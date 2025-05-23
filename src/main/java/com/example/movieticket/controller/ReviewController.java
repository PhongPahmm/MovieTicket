package com.example.movieticket.controller;

import com.example.movieticket.dto.request.ReviewRequest;
import com.example.movieticket.dto.response.MovieReviewResponse;
import com.example.movieticket.dto.response.ResponseData;
import com.example.movieticket.dto.response.ReviewResponse;
import com.example.movieticket.service.ReviewService;
import com.example.movieticket.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("reviews")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewController {
    ReviewService reviewService;
    UserService userService;

    @PostMapping("/add-review")
    public ReviewResponse addReview(@RequestBody ReviewRequest request) {
        var user = userService.getCurrentUser().getId();
        return reviewService.createReview(user,request);
    }
    @GetMapping("")
    public ResponseData<List<ReviewResponse>> getAllReviews() {
        return ResponseData.<List<ReviewResponse>>builder()
                .code(200)
                .message("Success")
                .data(reviewService.getAllReviews())
                .build();
    }
    @GetMapping("/movie/{movieId}")
    public ResponseData<MovieReviewResponse> getReviewByMovieId(
            @PathVariable int movieId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var reviews = reviewService.getReviewByMovieId(movieId, page, size);
        return ResponseData.<MovieReviewResponse>builder()
                .code(200)
                .data(reviews)
                .message("Success")
                .build();
    }

    @PutMapping("/movie/{reviewId}")
    public ReviewResponse updateReview(@PathVariable int reviewId, @RequestBody ReviewRequest request) {
        return reviewService.updateReview(reviewId, request);
    }
    @DeleteMapping("/movie/{reviewId}")
    public ResponseData<Void> deleteReview(@PathVariable int reviewId) {
        return ResponseData.<Void>builder()
                .code(200)
                .message("Successfully deleted review")
                .data(reviewService.deleteReview(reviewId))
                .build();
    }
}

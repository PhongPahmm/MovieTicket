package com.example.movieticket.service.impl;

import com.example.movieticket.common.UserRole;
import com.example.movieticket.dto.request.ReviewRequest;
import com.example.movieticket.dto.response.MovieReviewResponse;
import com.example.movieticket.dto.response.ReviewResponse;
import com.example.movieticket.exception.AppException;
import com.example.movieticket.exception.ErrorCode;
import com.example.movieticket.model.Review;
import com.example.movieticket.repository.MovieRepository;
import com.example.movieticket.repository.ReviewRepository;
import com.example.movieticket.repository.UserRepository;
import com.example.movieticket.service.ReviewService;
import com.example.movieticket.service.UserService;
import com.example.movieticket.util.PaginationUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewServiceImpl implements ReviewService {
    ReviewRepository reviewRepository;
    UserRepository userRepository;
    MovieRepository movieRepository;
    private final UserService userService;

    @Override
    public ReviewResponse createReview(int userId, ReviewRequest request) {
        var user = userRepository.findById(userId)
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));
        var movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(()-> new AppException(ErrorCode.MOVIE_NOT_FOUND));
        boolean alreadyReviewed = reviewRepository.existsByUserAndMovie(user, movie);
        if (alreadyReviewed) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }
        var review = Review.builder()
                .user(user)
                .movie(movie)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();
        var savedReview = reviewRepository.save(review);
        return mapToReviewResponse(savedReview);
    }

    @Override
    public ReviewResponse updateReview(int reviewId, ReviewRequest request) {
        var currentUser = userService.getCurrentUser();
        var review = reviewRepository.findById(reviewId)
                .orElseThrow(()-> new AppException(ErrorCode.REVIEW_NOT_FOUND));
        boolean isOwner = currentUser.getId().equals(review.getUser().getId());
        boolean isAdmin = currentUser.getRole().equals(UserRole.ADMIN);

        if (!isOwner && !isAdmin) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        if(review.getMovie().getId() != request.getMovieId()) {
            throw new AppException(ErrorCode.MOVIE_NOT_FOUND);
        }
        if(review.getRating() != null){
            review.setRating(request.getRating());
        }
        if(review.getComment() != null){
            review.setComment(request.getComment());
        }
        return mapToReviewResponse(reviewRepository.save(review));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<ReviewResponse> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(this::mapToReviewResponse)
                .toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Void deleteReview(int reviewId) {
        var review = reviewRepository.findById(reviewId)
                .orElseThrow(()-> new AppException(ErrorCode.REVIEW_NOT_FOUND));
        reviewRepository.delete(review);
        return null;
    }

    @Override
    public MovieReviewResponse getReviewByMovieId(Integer movieId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        var movie = movieRepository.findById(movieId)
                .orElseThrow(()-> new AppException(ErrorCode.MOVIE_NOT_FOUND));
        var reviews = reviewRepository.findByMovie(movie, pageable);
        var averageRating = getAverageRating(movieId);

        return MovieReviewResponse.builder()
                .averageRating(averageRating)
                .reviews(PaginationUtil.mapToPageResponse(reviews, this::mapToReviewResponse))
                .build();
    }

    private Double getAverageRating(Integer movieId) {
        var movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));

        var reviews = reviewRepository.findByMovie(movie);

        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    private ReviewResponse mapToReviewResponse(Review review) {
        return ReviewResponse.builder()
                .reviewId(review.getId())
                .userId(review.getUser().getId())
                .movieId(review.getMovie().getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}

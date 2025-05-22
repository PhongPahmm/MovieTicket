package com.example.movieticket.repository;

import com.example.movieticket.model.Movie;
import com.example.movieticket.model.Review;
import com.example.movieticket.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByMovie(Movie movie);
    boolean existsByUserAndMovie(User user, Movie movie);
}

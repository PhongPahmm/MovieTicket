package com.example.movieticket.repository;

import com.example.movieticket.common.MovieStatus;
import com.example.movieticket.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Integer> {
    List<Movie> findAllByStatus(MovieStatus status);
    List<Movie> findAllByReleaseDate(LocalDate releaseDate);
}

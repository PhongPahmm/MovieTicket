package com.example.movieticket.repository;

import com.example.movieticket.common.MovieStatus;
import com.example.movieticket.model.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Integer> {
    Page<Movie> findAllByStatus(MovieStatus status, Pageable pageable);
    Page<Movie> findAllByReleaseDate(LocalDate releaseDate, Pageable pageable);
    @Query("SELECT DISTINCT mg.movie FROM MovieGenre mg " +
            "WHERE (:genreIds IS NULL OR mg.genre.id IN :genreIds)")
    Page<Movie> findMovieByGenreIds(@Param("genreIds") List<Integer> genreIds, Pageable pageable);
}

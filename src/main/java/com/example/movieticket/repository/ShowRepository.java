package com.example.movieticket.repository;

import com.example.movieticket.model.Movie;
import com.example.movieticket.model.Screen;
import com.example.movieticket.model.Show;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;

@Repository
public interface ShowRepository extends JpaRepository<Show, Integer> {
    Page<Show> findByMovie(Movie movie, Pageable pageable);

    @Query("""
    SELECT COUNT(s) > 0 FROM Show s
    WHERE s.screen = :screen
      AND s.showDate = :showDate
      AND (
        (:startTime BETWEEN s.startTime AND s.endTime)
        OR (:endTime BETWEEN s.startTime AND s.endTime)
        OR (s.startTime BETWEEN :startTime AND :endTime)
      )
""")
    boolean existsByScreenAndShowDateAndTimeOverlap(
            @Param("screen") Screen screen,
            @Param("showDate") LocalDate showDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    Page<Show> findByMovieAndActiveTrue(Movie movie, Pageable pageable);
}

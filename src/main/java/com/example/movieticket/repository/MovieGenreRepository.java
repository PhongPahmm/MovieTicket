package com.example.movieticket.repository;

import com.example.movieticket.model.Movie;
import com.example.movieticket.model.MovieGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieGenreRepository extends JpaRepository<MovieGenre, Integer> {
    List<MovieGenre> findByMovie(Movie movie);
}

package com.example.movieticket.service;

import com.example.movieticket.dto.request.MovieRequest;
import com.example.movieticket.dto.response.MovieResponse;

import java.time.LocalDate;
import java.util.List;

public interface MovieService {
    MovieResponse createMovie(MovieRequest request);
    MovieResponse updateMovie(int movieId, MovieRequest request);
    List<MovieResponse> getAllMovies();
    MovieResponse getMovieById(int movieId);
    MovieResponse changeMovieStatus(int movieId, boolean isActive);
    List<MovieResponse> getAllNowShowingMovies();
    List<MovieResponse> getAllComingSoonMovies();
    List<MovieResponse> getMovieByReleaseDate(LocalDate releaseDate);
    List<MovieResponse> getMovieByGenre(List<Integer> genreId);
}

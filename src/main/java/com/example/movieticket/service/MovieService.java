package com.example.movieticket.service;

import com.example.movieticket.dto.request.MovieRequest;
import com.example.movieticket.dto.response.MovieResponse;
import com.example.movieticket.dto.response.PageResponse;

import java.time.LocalDate;
import java.util.List;

public interface MovieService {
    MovieResponse createMovie(MovieRequest request);
    MovieResponse updateMovie(int movieId, MovieRequest request);
    PageResponse<MovieResponse> getAllMovies(int page, int size);
    MovieResponse getMovieById(int movieId);
    MovieResponse changeMovieStatus(int movieId, boolean isActive);
    PageResponse<MovieResponse> getAllNowShowingMovies(int page, int size);
    PageResponse<MovieResponse> getAllComingSoonMovies(int page, int size);
    PageResponse<MovieResponse> getMovieByReleaseDate(LocalDate releaseDate, int page, int size);
    PageResponse<MovieResponse> getMovieByGenre(List<Integer> genreId, int page, int size);
}

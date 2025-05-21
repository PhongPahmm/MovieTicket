package com.example.movieticket.service.impl;

import com.example.movieticket.common.MovieStatus;
import com.example.movieticket.dto.request.MovieRequest;
import com.example.movieticket.dto.response.MovieResponse;
import com.example.movieticket.exception.AppException;
import com.example.movieticket.exception.ErrorCode;
import com.example.movieticket.model.Movie;
import com.example.movieticket.repository.MovieRepository;
import com.example.movieticket.service.FileStorageService;
import com.example.movieticket.service.MovieService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MovieServiceImpl implements MovieService {
    MovieRepository movieRepository;
    FileStorageService fileStorageService;
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public MovieResponse createMovie(MovieRequest request) {
        String posterUrl = "";
        if(request.getPosterUrl() != null) {
            posterUrl = fileStorageService.upload(request.getPosterUrl());
        }
        var movie = Movie.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .durationMinutes(request.getDurationMinutes())
                .releaseDate(request.getReleaseDate())
                .ageRating(request.getAgeRating())
                .director(request.getDirector())
                .actors(request.getActors())
                .language(request.getLanguage())
                .posterUrl(posterUrl)
                .trailerUrl(request.getTrailerUrl())
                .status(request.getStatus())
                .active(true)
                .build();
        var savedMovie = movieRepository.save(movie);
        return mapToMovieResponse(savedMovie);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public MovieResponse updateMovie(int movieId, MovieRequest request) {
        var movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));

        if (request.getPosterUrl() != null) {
            String posterUrl = fileStorageService.upload(request.getPosterUrl());
            movie.setPosterUrl(posterUrl);
        }

        if (request.getTitle() != null) movie.setTitle(request.getTitle());
        if (request.getDescription() != null) movie.setDescription(request.getDescription());
        if (request.getDurationMinutes() != null) movie.setDurationMinutes(request.getDurationMinutes());
        if (request.getReleaseDate() != null) movie.setReleaseDate(request.getReleaseDate());
        if (request.getAgeRating() != null) movie.setAgeRating(request.getAgeRating());
        if (request.getDirector() != null) movie.setDirector(request.getDirector());
        if (request.getActors() != null) movie.setActors(request.getActors());
        if (request.getLanguage() != null) movie.setLanguage(request.getLanguage());
        if (request.getTrailerUrl() != null) movie.setTrailerUrl(request.getTrailerUrl());
        if (request.getStatus() != null) movie.setStatus(request.getStatus());
        if (request.getActive() != null) movie.setActive(request.getActive());

        var savedMovie = movieRepository.save(movie);
        return mapToMovieResponse(savedMovie);
    }

    @Override
    public List<MovieResponse> getAllMovies() {
        return movieRepository.findAll()
                .stream().map(this::mapToMovieResponse)
                .toList();
    }

    @Override
    public MovieResponse getMovieById(int movieId) {
        var movie = movieRepository.findById(movieId)
                .orElseThrow(()-> new AppException(ErrorCode.MOVIE_NOT_FOUND));
        return mapToMovieResponse(movie);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public MovieResponse changeMovieStatus(int movieId, boolean isActive) {
        var movie = movieRepository.findById(movieId)
                .orElseThrow(()-> new AppException(ErrorCode.MOVIE_NOT_FOUND));
        movie.setActive(isActive);
        return mapToMovieResponse(movieRepository.save(movie));
    }

    @Override
    public List<MovieResponse> getAllNowShowingMovies() {
       var movies = movieRepository.findAllByStatus(MovieStatus.NOW_SHOWING);
         return movies.stream()
                 .map(this::mapToMovieResponse)
                 .toList();
    }

    @Override
    public List<MovieResponse> getAllComingSoonMovies() {
        var movies = movieRepository.findAllByStatus(MovieStatus.COMING_SOON);
        return movies.stream()
                .map(this::mapToMovieResponse)
                .toList();
    }

    @Override
    public List<MovieResponse> getMovieByReleaseDate(LocalDate releaseDate) {
        return movieRepository.findAllByReleaseDate(releaseDate)
                .stream().map(this::mapToMovieResponse)
                .toList();
    }

    private MovieResponse mapToMovieResponse(Movie movie) {
        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .description(movie.getDescription())
                .durationMinutes(movie.getDurationMinutes())
                .releaseDate(movie.getReleaseDate())
                .ageRating(movie.getAgeRating())
                .director(movie.getDirector())
                .actors(movie.getActors())
                .language(movie.getLanguage())
                .posterUrl(movie.getPosterUrl())
                .trailerUrl(movie.getTrailerUrl())
                .status(movie.getStatus())
                .createdAt(movie.getCreatedAt())
                .updatedAt(movie.getUpdatedAt())
                .active(movie.getActive())
                .build();
    }
}

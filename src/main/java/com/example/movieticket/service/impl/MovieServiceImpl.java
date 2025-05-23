package com.example.movieticket.service.impl;

import com.example.movieticket.common.MovieStatus;
import com.example.movieticket.dto.request.MovieRequest;
import com.example.movieticket.dto.response.GenreResponse;
import com.example.movieticket.dto.response.MovieResponse;
import com.example.movieticket.dto.response.PageResponse;
import com.example.movieticket.exception.AppException;
import com.example.movieticket.exception.ErrorCode;
import com.example.movieticket.model.Genre;
import com.example.movieticket.model.Movie;
import com.example.movieticket.model.MovieGenre;
import com.example.movieticket.repository.GenreRepository;
import com.example.movieticket.repository.MovieGenreRepository;
import com.example.movieticket.repository.MovieRepository;
import com.example.movieticket.service.FileStorageService;
import com.example.movieticket.service.MovieService;
import com.example.movieticket.util.PaginationUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    MovieGenreRepository movieGenreRepository;
    GenreRepository genreRepository;

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
        if(request.getGenreIds() != null) {
            List<MovieGenre> movieGenres = request.getGenreIds()
                    .stream().map(id -> {
                        Genre genre =  genreRepository.findById(id)
                                .orElseThrow(()-> new RuntimeException("Genre not found"));
                        return MovieGenre.builder()
                                .movie(savedMovie)
                                .genre(genre)
                                .build();
                    })
                    .toList();
            movieGenreRepository.saveAll(movieGenres);
        }
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
    public PageResponse<MovieResponse> getAllMovies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        var movies = movieRepository.findAll(pageable);
        return PaginationUtil.mapToPageResponse(movies, this::mapToMovieResponse);
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
    public PageResponse<MovieResponse> getAllNowShowingMovies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
       var movies = movieRepository.findAllByStatus(MovieStatus.NOW_SHOWING, pageable);
         return PaginationUtil.mapToPageResponse(movies, this::mapToMovieResponse);
    }

    @Override
    public PageResponse<MovieResponse> getAllComingSoonMovies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        var movies = movieRepository.findAllByStatus(MovieStatus.COMING_SOON, pageable);
        return PaginationUtil.mapToPageResponse(movies, this::mapToMovieResponse);
    }

    @Override
    public PageResponse<MovieResponse> getMovieByReleaseDate(LocalDate releaseDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        var movies = movieRepository.findAllByReleaseDate(releaseDate, pageable);
        return PaginationUtil.mapToPageResponse(movies, this::mapToMovieResponse);
    }

    @Override
    public PageResponse<MovieResponse> getMovieByGenre(List<Integer> genreId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        var movies =  movieRepository.findMovieByGenreIds(genreId, pageable);
        return PaginationUtil.mapToPageResponse(movies, this::mapToMovieResponse);
    }

    private MovieResponse mapToMovieResponse(Movie movie) {
        List<GenreResponse> genreInfos = movieGenreRepository.findByMovie(movie)
                .stream().map(mv ->{
                    Genre genre = mv.getGenre();
                    return GenreResponse.builder()
                            .id(genre.getId())
                            .name(genre.getName())
                            .build();
                })
                .toList();
        return MovieResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .description(movie.getDescription())
                .genres(genreInfos)
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

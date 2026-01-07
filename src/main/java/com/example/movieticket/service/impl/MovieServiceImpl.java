package com.example.movieticket.service.impl;

import com.example.movieticket.common.MovieStatus;
import com.example.movieticket.common.UserRole;
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
import com.example.movieticket.service.CloudinaryService;
import com.example.movieticket.service.MovieService;
import com.example.movieticket.service.UserService;
import com.example.movieticket.util.PaginationUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MovieServiceImpl implements MovieService {
    MovieRepository movieRepository;
    CloudinaryService cloudinaryService;
    MovieGenreRepository movieGenreRepository;
    GenreRepository genreRepository;
    private final UserService userService;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @CacheEvict(value = "allMovies", allEntries = true)
    public MovieResponse createMovie(MovieRequest request) {
        String posterUrl = "";
        if(request.getPosterUrl() != null) {
            posterUrl = cloudinaryService.upload(request.getPosterUrl());
        }
        String trailerUrl = "";
        if(request.getTrailerUrl() != null) {
            trailerUrl = cloudinaryService.upload(request.getTrailerUrl());
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
                .trailerUrl(trailerUrl)
                .status(request.getStatus())
                .active(request.getActive() == null ? Boolean.TRUE : request.getActive())
                .build();

        autoUpdateStatus(movie);

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
    @CacheEvict(value = "allMovies", allEntries = true)
    public MovieResponse updateMovie(int movieId, MovieRequest request) {
        var movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));

        if (request.getPosterUrl() != null) {
            String posterUrl = cloudinaryService.upload(request.getPosterUrl());
            movie.setPosterUrl(posterUrl);
        }
        if (request.getTrailerUrl() != null) {
            String trailerUrl = cloudinaryService.upload(request.getTrailerUrl());
            movie.setTrailerUrl(trailerUrl);
        }
        if (request.getTitle() != null) movie.setTitle(request.getTitle());
        if (request.getDescription() != null) movie.setDescription(request.getDescription());
        if (request.getDurationMinutes() != null) movie.setDurationMinutes(request.getDurationMinutes());
        if (request.getReleaseDate() != null) movie.setReleaseDate(request.getReleaseDate());
        if (request.getAgeRating() != null) movie.setAgeRating(request.getAgeRating());
        if (request.getDirector() != null) movie.setDirector(request.getDirector());
        if (request.getActors() != null) movie.setActors(request.getActors());
        if (request.getLanguage() != null) movie.setLanguage(request.getLanguage());
        if (request.getStatus() != null) {
            movie.setStatus(request.getStatus());
        }
        if (request.getActive() != null) movie.setActive(request.getActive());

        autoUpdateStatus(movie);

        var savedMovie = movieRepository.save(movie);
        return mapToMovieResponse(savedMovie);
    }

    @Override
    @Cacheable(value = "allMovies", key ="'page:' + #page +'size' + #size + 'role' + #role")
    public PageResponse<MovieResponse> getAllMovies(int page, int size, UserRole role) {
        System.out.println("getAllMovies from database!!!!");
        refreshMovieStatuses();
        Pageable pageable = PageRequest.of(page, size);
        var user = userService.getCurrentUser();
        Page<Movie> movies;

        if(user != null && user.getRole().equals(UserRole.ADMIN)){
            movies = movieRepository.findAll(pageable);
        }else {
            movies = movieRepository.findAllByActiveTrue(pageable);
        }

        return PaginationUtil.mapToPageResponse(movies, this::mapToMovieResponse);
    }

    @Override
    public MovieResponse getMovieById(int movieId) {
        refreshMovieStatuses();
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
        refreshMovieStatuses();
        Pageable pageable = PageRequest.of(page, size);
        var user = userService.getCurrentUser();
        Page<Movie> movies;
        if (user != null && user.getRole().equals(UserRole.ADMIN)){
            movies = movieRepository.findAllByStatus(MovieStatus.NOW_SHOWING, pageable);
        } else {
            movies = movieRepository.findAllByStatusAndActiveTrue(MovieStatus.NOW_SHOWING, pageable);
        }
         return PaginationUtil.mapToPageResponse(movies, this::mapToMovieResponse);
    }

    @Override
    public PageResponse<MovieResponse> getAllComingSoonMovies(int page, int size) {
        refreshMovieStatuses();
        Pageable pageable = PageRequest.of(page, size);
        var user = userService.getCurrentUser();
        Page<Movie> movies;
        if(user != null && user.getRole().equals(UserRole.ADMIN)){
            movies = movieRepository.findAllByStatus(MovieStatus.COMING_SOON, pageable);
        }else {
            movies = movieRepository.findAllByStatusAndActiveTrue(MovieStatus.COMING_SOON, pageable);
        }

        return PaginationUtil.mapToPageResponse(movies, this::mapToMovieResponse);
    }

    @Override
    public PageResponse<MovieResponse> getMovieByReleaseDate(LocalDate releaseDate, int page, int size) {
        refreshMovieStatuses();
        Pageable pageable = PageRequest.of(page, size);
        var user = userService.getCurrentUser();
        Page<Movie> movies;
        if (user != null && user.getRole().equals(UserRole.ADMIN)){
            movies = movieRepository.findAllByReleaseDate(releaseDate, pageable);
        }else {
            movies = movieRepository.findAllByReleaseDateAndActiveTrue(releaseDate, pageable);
        }
        return PaginationUtil.mapToPageResponse(movies, this::mapToMovieResponse);
    }

    @Override
    public PageResponse<MovieResponse> getMovieByGenre(List<Integer> genreId, int page, int size) {
        refreshMovieStatuses();
        Pageable pageable = PageRequest.of(page, size);
        var user = userService.getCurrentUser();
        Page<Movie> movies;
        if (user != null && user.getRole().equals(UserRole.ADMIN)){
            movies =  movieRepository.findMovieByGenreIds(genreId, pageable);
        }else {
            movies = movieRepository.findMovieByGenreIdsAndActiveTrue(genreId, pageable);
        }

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
                .actors(new ArrayList<>(movie.getActors()))
                .language(movie.getLanguage())
                .posterUrl(movie.getPosterUrl())
                .trailerUrl(movie.getTrailerUrl())
                .status(movie.getStatus())
                .createdAt(movie.getCreatedAt())
                .updatedAt(movie.getUpdatedAt())
                .active(movie.getActive())
                .build();
    }

    private void autoUpdateStatus(Movie movie) {
        LocalDate releaseDate = movie.getReleaseDate();
        if (releaseDate == null) {
            if (movie.getStatus() == null) {
                movie.setStatus(MovieStatus.COMING_SOON);
            }
            return;
        }

        LocalDate today = LocalDate.now();
        if (!releaseDate.isAfter(today)) {
            if (movie.getStatus() != MovieStatus.NOW_SHOWING) {
                movie.setStatus(MovieStatus.NOW_SHOWING);
            }
        } else {
            if (movie.getStatus() != MovieStatus.COMING_SOON) {
                movie.setStatus(MovieStatus.COMING_SOON);
            }
        }
    }

    private void refreshMovieStatuses() {
        LocalDate today = LocalDate.now();
        List<Movie> moviesToUpdate = new ArrayList<>();

        List<Movie> movies = movieRepository.findAll();
        for (Movie movie : movies) {
            LocalDate releaseDate = movie.getReleaseDate();
            if (releaseDate == null) {
                if (movie.getStatus() == null) {
                    movie.setStatus(MovieStatus.COMING_SOON);
                    moviesToUpdate.add(movie);
                }
                continue;
            }

            boolean shouldBeNowShowing = !releaseDate.isAfter(today);
            MovieStatus desiredStatus = shouldBeNowShowing ? MovieStatus.NOW_SHOWING : MovieStatus.COMING_SOON;
            if (movie.getStatus() != desiredStatus) {
                movie.setStatus(desiredStatus);
                moviesToUpdate.add(movie);
            }
        }

        if (!moviesToUpdate.isEmpty()) {
            movieRepository.saveAll(moviesToUpdate);
        }
    }
}

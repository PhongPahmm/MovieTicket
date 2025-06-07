package com.example.movieticket.controller;

import com.example.movieticket.dto.request.MovieRequest;
import com.example.movieticket.dto.response.MovieResponse;
import com.example.movieticket.dto.response.PageResponse;
import com.example.movieticket.dto.response.ResponseData;
import com.example.movieticket.service.MovieService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MovieController {
    MovieService movieService;

    @PostMapping(value = "add-movie", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseData<MovieResponse> createMovie(@ModelAttribute MovieRequest request) {
        return ResponseData.<MovieResponse>builder()
                .code(200)
                .message("Success creating movie")
                .data(movieService.createMovie(request))
                .build();
    }
    @PutMapping(value = "update-movie/{movieId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseData<MovieResponse> updateMovie(@PathVariable int movieId, @ModelAttribute MovieRequest request) {
        return ResponseData.<MovieResponse>builder()
                .code(200)
                .message("Successfully updated movie")
                .data(movieService.updateMovie(movieId, request))
                .build();
    }
    @GetMapping("/{movieId}")
    public ResponseData<MovieResponse> getMovieById(@PathVariable int movieId) {
        return ResponseData.<MovieResponse>builder()
                .code(200)
                .message("Successfully retrieved movie")
                .data(movieService.getMovieById(movieId))
                .build();
    }
    @GetMapping("")
    public ResponseData<PageResponse<MovieResponse>> getAllMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        return ResponseData.<PageResponse<MovieResponse>>builder()
                .code(200)
                .data(movieService.getAllMovies(page, size))
                .message("Successfully retrieved all movies")
                .build();
    }
    @PutMapping("status/{movieId}")
    public ResponseData<MovieResponse> changeMovieStatus(@PathVariable int movieId, @RequestParam boolean isActive){
        return ResponseData.<MovieResponse>builder()
                .code(200)
                .message("Movie status changed")
                .data(movieService.changeMovieStatus(movieId, isActive))
                .build();
    }
    @GetMapping("/now-showing")
    public ResponseData<PageResponse<MovieResponse>> getNowShowingMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size
    ){
        return ResponseData.<PageResponse<MovieResponse>>builder()
                .code(200)
                .message("Successfully retrieved now showing movies")
                .data(movieService.getAllNowShowingMovies(page, size))
                .build();
    }
    @GetMapping("/coming-soon")
    public ResponseData<PageResponse<MovieResponse>> getComingSoonMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size
    ){
        return ResponseData.<PageResponse<MovieResponse>>builder()
                .code(200)
                .message("Successfully retrieved coming soon movies")
                .data(movieService.getAllComingSoonMovies(page, size))
                .build();
    }
    @GetMapping("/release-date")
    public ResponseData<PageResponse<MovieResponse>> getMovieByReleaseDate(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam("release-date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate releaseDate) {
        return ResponseData.<PageResponse<MovieResponse>>builder()
                .code(200)
                .message("Successfully retrieved movies")
                .data(movieService.getMovieByReleaseDate(releaseDate, page, size))
                .build();
    }
    @PostMapping("/find-by-genre")
    public ResponseData<PageResponse<MovieResponse>> getMovieByGenre(
            @RequestBody List<Integer> genreIds,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        return ResponseData.<PageResponse<MovieResponse>>builder()
                .code(200)
                .message("Successfully retrieved movies")
                .data(movieService.getMovieByGenre(genreIds, page, size))
                .build();
    }

}

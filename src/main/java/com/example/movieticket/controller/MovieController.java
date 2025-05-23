package com.example.movieticket.controller;

import com.example.movieticket.dto.request.MovieRequest;
import com.example.movieticket.dto.response.MovieResponse;
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
    public ResponseData<List<MovieResponse>> getAllUsers(){
        return ResponseData.<List<MovieResponse>>builder()
                .code(200)
                .data(movieService.getAllMovies())
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
    public ResponseData<List<MovieResponse>> getNowShowingMovies(){
        return ResponseData.<List<MovieResponse>>builder()
                .code(200)
                .message("Successfully retrieved now showing movies")
                .data(movieService.getAllNowShowingMovies())
                .build();
    }
    @GetMapping("/coming-soon")
    public ResponseData<List<MovieResponse>> getComingSoonMovies(){
        return ResponseData.<List<MovieResponse>>builder()
                .code(200)
                .message("Successfully retrieved now showing movies")
                .data(movieService.getAllComingSoonMovies())
                .build();
    }
    @GetMapping("/release-date")
    public ResponseData<List<MovieResponse>> getMovieByReleaseDate(
            @RequestParam("release-date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate releaseDate) {
        return ResponseData.<List<MovieResponse>>builder()
                .code(200)
                .message("Successfully retrieved movies")
                .data(movieService.getMovieByReleaseDate(releaseDate))
                .build();
    }
    @PostMapping("/find-by-genre")
    public ResponseData<List<MovieResponse>> getMovieByGenre(@RequestBody List<Integer> genreIds) {
        return ResponseData.<List<MovieResponse>>builder()
                .code(200)
                .message("Successfully retrieved movies")
                .data(movieService.getMovieByGenre(genreIds))
                .build();
    }

}

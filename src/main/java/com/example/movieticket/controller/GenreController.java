package com.example.movieticket.controller;


import com.example.movieticket.dto.request.GenreRequest;
import com.example.movieticket.dto.response.GenreResponse;
import com.example.movieticket.dto.response.ResponseData;
import com.example.movieticket.service.GenreService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GenreController {
    GenreService genreService;

    @PostMapping("/add-genre")
    public ResponseData<GenreResponse> addGenre(@RequestBody GenreRequest request) {
        return ResponseData.<GenreResponse>builder()
                .code(200)
                .message("Success")
                .data(genreService.createGenre(request))
                .build();
    }
    @GetMapping("")
    public ResponseData<List<GenreResponse>> getGenres() {
        return ResponseData.<List<GenreResponse>>builder()
                .code(200)
                .message("Success")
                .data(genreService.getAllGenres())
                .build();
    }
    @DeleteMapping("/{genreId}")
    public ResponseData<Void> deleteGenre(@PathVariable Integer genreId) {
        return ResponseData.<Void>builder()
                .code(200)
                .message("Success")
                .data(genreService.deleteGenre(genreId))
                .build();
    }
}

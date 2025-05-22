package com.example.movieticket.service;

import com.example.movieticket.dto.request.GenreRequest;
import com.example.movieticket.dto.response.GenreResponse;

import java.util.List;

public interface GenreService {
    GenreResponse createGenre(GenreRequest request);
    Void deleteGenre(int genreId );
    GenreResponse getGenre(int genreId);
    List<GenreResponse> getAllGenres();
}

package com.example.movieticket.service.impl;

import com.example.movieticket.dto.request.GenreRequest;
import com.example.movieticket.dto.response.GenreResponse;
import com.example.movieticket.exception.AppException;
import com.example.movieticket.exception.ErrorCode;
import com.example.movieticket.model.Genre;
import com.example.movieticket.repository.GenreRepository;
import com.example.movieticket.service.GenreService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GenreServiceImpl implements GenreService {
    GenreRepository genreRepository;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public GenreResponse createGenre(GenreRequest request) {
        var genre = Genre.builder()
                .name(request.getName())
                .build();
        if (genreRepository.existsByName(genre.getName())) {
            throw new AppException(ErrorCode.GENRE_EXISTED);
        }
        return maptoGenreResponse(genreRepository.save(genre));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Void deleteGenre(int genreId) {
        genreRepository.deleteById(genreId);
        return null;
    }

    @Override
    public GenreResponse getGenre(int genreId) {
        var genre = genreRepository.findById(genreId)
                .orElseThrow(()->new RuntimeException("Genre not found"));
        return maptoGenreResponse(genre);
    }

    @Override
    public List<GenreResponse> getAllGenres() {
        return genreRepository.findAll()
                .stream().map(this::maptoGenreResponse)
                .toList();
    }
    private GenreResponse maptoGenreResponse(Genre genre) {
        return GenreResponse.builder()
                .id(genre.getId())
                .name(genre.getName())
                .build();
    }
}

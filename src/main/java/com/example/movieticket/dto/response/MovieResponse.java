package com.example.movieticket.dto.response;

import com.example.movieticket.common.MovieStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class MovieResponse {
    private int id;
    private String title;
    private String description;
    private Integer durationMinutes;
    private LocalDate releaseDate;
    private String ageRating;
//    private List<GenreResponse> genre;
    private String director;
    private List<String> actors;
    private String language;
    private String posterUrl;
    private String trailerUrl;
    private Boolean active;
    private MovieStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

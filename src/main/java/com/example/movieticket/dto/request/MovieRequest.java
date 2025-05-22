package com.example.movieticket.dto.request;

import com.example.movieticket.common.MovieStatus;
import com.example.movieticket.dto.response.GenreResponse;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class MovieRequest {
    private String title;
    private String description;
    private Integer durationMinutes;
    private LocalDate releaseDate;
    private String ageRating;
    private String director;
    private List<String> actors;
    private String language;
    private MultipartFile posterUrl;
    private String trailerUrl;
    private Boolean active;
    private MovieStatus status;
    List<Integer> genreIds;
}

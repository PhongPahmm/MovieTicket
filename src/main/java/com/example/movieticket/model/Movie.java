package com.example.movieticket.model;

import com.example.movieticket.common.MovieStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "movies")
public class Movie extends AbstractEntity<Integer> {
    private String title;
    private String description;
    private int durationMinutes;
    private LocalDate releaseDate;
    private String ageRating;
    private String genre;
    private String director;
    private List<String> actors;
    private String language;
    private String posterUrl;
    private String trailerUrl;
    private boolean active;

    @Enumerated(EnumType.STRING)
    private MovieStatus status;
}

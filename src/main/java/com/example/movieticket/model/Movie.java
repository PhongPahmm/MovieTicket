package com.example.movieticket.model;

import com.example.movieticket.common.MovieStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
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
public class Movie extends AbstractEntity<Integer> implements Serializable {
    private String title;
    private String description;
    private Integer durationMinutes;
    private LocalDate releaseDate;
    private String ageRating;
    private String director;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> actors;

    private String language;
    private String posterUrl;
    private String trailerUrl;
    private Boolean active;

    @Enumerated(EnumType.STRING)
    private MovieStatus status;
}

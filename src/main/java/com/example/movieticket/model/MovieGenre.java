package com.example.movieticket.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "movie_genres")
public class MovieGenre extends AbstractEntity<Integer> {
    @ManyToOne
    @JoinColumn(name = "movie_id")
    Movie movie;
    @ManyToOne
    @JoinColumn(name = "genre_id")
    Genre genre;
}

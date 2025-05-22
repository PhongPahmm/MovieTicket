package com.example.movieticket.repository;

import com.example.movieticket.model.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Integer> {
    Boolean existsByName(String name);
}

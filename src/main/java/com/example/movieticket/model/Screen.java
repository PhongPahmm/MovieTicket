package com.example.movieticket.model;

import jakarta.persistence.Entity;
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
@Table(name = "screens")
public class Screen extends AbstractEntity<Integer> {
    String name;
    int totalSeats;
    boolean is3D;
    boolean active;
}

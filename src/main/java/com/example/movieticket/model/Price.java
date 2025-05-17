package com.example.movieticket.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "prices")
public class Price extends AbstractEntity<Integer> {
    @ManyToOne
    @JoinColumn(name = "show_id")
    Show show;
    int amount;
    LocalDate validFrom;
    LocalDate validTo;
}

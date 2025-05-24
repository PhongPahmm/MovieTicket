package com.example.movieticket.model;

import com.example.movieticket.common.SeatType;
import jakarta.persistence.*;
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
    Integer amount;
    @Enumerated(EnumType.STRING)
    SeatType seatType;
    LocalDate validFrom;
    LocalDate validTo;
}

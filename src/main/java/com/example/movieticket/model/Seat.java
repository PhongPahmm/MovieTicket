package com.example.movieticket.model;

import com.example.movieticket.common.SeatType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "seats")
public class Seat extends AbstractEntity<Integer> {
    @ManyToOne
    @JoinColumn(name = "screen_id")
    Screen screen;
    int seatRow;
    int number;

    @Enumerated(EnumType.STRING)
    SeatType seatType;

}

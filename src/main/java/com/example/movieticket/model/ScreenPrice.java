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
@Table(name = "screen_prices")
public class ScreenPrice extends AbstractEntity<Integer> {
    @ManyToOne
    @JoinColumn(name = "screen_id")
    Screen screen;

    @Enumerated(EnumType.STRING)
    SeatType seatType;

    Integer amount;
}



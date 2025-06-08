package com.example.movieticket.model;

import com.example.movieticket.common.SeatStatus;
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
@Table(name = "booking_seats")
public class BookingSeat extends AbstractEntity<Integer> {
    @ManyToOne
    @JoinColumn(name = "booking_id")
    Booking booking;

    @ManyToOne
    @JoinColumn(name = "seat_id")
    Seat seat;

    int price;

    @Enumerated(EnumType.STRING)
    private SeatStatus status = SeatStatus.AVAILABLE;
}

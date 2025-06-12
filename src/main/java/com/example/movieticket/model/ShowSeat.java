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
@Table(name = "show_seats")
public class ShowSeat extends AbstractEntity<Integer> {

    @ManyToOne(fetch = FetchType.LAZY)
    private Show show;

    @ManyToOne(fetch = FetchType.LAZY)
    private Seat seat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status;
}


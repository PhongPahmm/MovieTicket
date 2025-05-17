package com.example.movieticket.model;

import com.example.movieticket.common.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "bookings")
public class Booking extends AbstractEntity<Integer> {
    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;
    @ManyToOne
    @JoinColumn(name = "show_id")
    Show show;

    LocalDateTime bookingTime;
    LocalDateTime expireTime;
    int totalAmount;

    @Enumerated(EnumType.STRING)
    BookingStatus status;

    @OneToOne
    @JoinColumn(name = "payment_id")
    Payment payment;
}

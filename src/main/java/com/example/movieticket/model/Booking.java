package com.example.movieticket.model;

import com.example.movieticket.common.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

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
    Double totalAmount;

    @Enumerated(EnumType.STRING)
    BookingStatus status;

    @OneToOne
    @JoinColumn(name = "payment_id")
    Payment payment;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<BookingSeat> bookingSeats;

}

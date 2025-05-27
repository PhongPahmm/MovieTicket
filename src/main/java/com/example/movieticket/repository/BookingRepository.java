package com.example.movieticket.repository;

import com.example.movieticket.common.BookingStatus;
import com.example.movieticket.model.Booking;
import com.example.movieticket.model.Payment;
import com.example.movieticket.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    List<Booking> findByStatusAndExpireTimeBefore(BookingStatus status, LocalDateTime now);

    Optional<Booking> findByPayment(Payment payment);

    List<Booking> findByUserOrderByBookingTimeDesc(User user);
}

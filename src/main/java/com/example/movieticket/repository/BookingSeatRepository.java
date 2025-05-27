package com.example.movieticket.repository;

import com.example.movieticket.model.Booking;
import com.example.movieticket.model.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, Integer> {
    List<BookingSeat> findByBookingShow_Id(Integer showId);

    boolean existsByBooking_Show_IdAndSeat_IdIn(Integer showId, List<Integer> seats);

    List<BookingSeat> findByBooking(Booking booking);
}

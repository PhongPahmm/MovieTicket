package com.example.movieticket.repository;

import com.example.movieticket.model.Booking;
import com.example.movieticket.model.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, Integer> {
    List<BookingSeat> findByBooking_Show_Id(Integer showId);

    List<BookingSeat> findByBooking(Booking booking);

    List<BookingSeat> findByBooking_Show_IdAndSeat_IdIn(Integer showId, List<Integer> seats);
}

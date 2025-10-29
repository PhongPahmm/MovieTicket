package com.example.movieticket.repository;

import com.example.movieticket.common.BookingStatus;
import com.example.movieticket.model.Booking;
import com.example.movieticket.model.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, Integer> {
    List<BookingSeat> findByBooking_Show_Id(Integer showId);

    List<BookingSeat> findByBooking(Booking booking);

    List<BookingSeat> findByBooking_Show_IdAndSeat_IdIn(Integer showId, List<Integer> seats);
    
    /**
     * Find booking seats for a show and seat IDs, excluding cancelled bookings
     */
    @Query("SELECT bs FROM BookingSeat bs " +
           "WHERE bs.booking.show.id = :showId " +
           "AND bs.seat.id IN :seatIds " +
           "AND bs.booking.status IN :statuses")
    List<BookingSeat> findByShowAndSeatsWithBookingStatus(
            @Param("showId") Integer showId,
            @Param("seatIds") List<Integer> seatIds,
            @Param("statuses") List<BookingStatus> statuses
    );
    
    /**
     * Find booking seats for a show, only from active bookings (PENDING or CONFIRMED)
     */
    @Query("SELECT bs FROM BookingSeat bs " +
           "WHERE bs.booking.show.id = :showId " +
           "AND bs.booking.status IN :statuses")
    List<BookingSeat> findByShowWithBookingStatus(
            @Param("showId") Integer showId,
            @Param("statuses") List<BookingStatus> statuses
    );
}

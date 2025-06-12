package com.example.movieticket.service.impl;

import com.example.movieticket.common.BookingStatus;
import com.example.movieticket.common.PaymentStatus;
import com.example.movieticket.common.SeatStatus;
import com.example.movieticket.model.Booking;
import com.example.movieticket.model.BookingSeat;
import com.example.movieticket.repository.BookingRepository;
import com.example.movieticket.repository.BookingSeatRepository;
import com.example.movieticket.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingCleanUpService {
    BookingRepository bookingRepository;
    PaymentRepository paymentRepository;
    BookingSeatRepository bookingSeatRepository;
    @Transactional
    public void cancelExpiredBookings() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> expiredBookings = bookingRepository.findByStatusAndExpireTimeBefore(BookingStatus.PENDING, now);

        for (Booking booking : expiredBookings) {
            booking.setStatus(BookingStatus.CANCELLED);

            List<BookingSeat> bookingSeats = bookingSeatRepository.findByBooking(booking);
            for (BookingSeat seat : bookingSeats) {
                seat.setStatus(SeatStatus.AVAILABLE);
            }
            bookingSeatRepository.saveAll(bookingSeats);

            if (booking.getPayment() != null) {
                booking.getPayment().setStatus(PaymentStatus.FAILED);
                paymentRepository.save(booking.getPayment());
            }
            bookingRepository.save(booking);
        }
    }
}

package com.example.movieticket.service;

import com.example.movieticket.dto.request.BookingRequest;
import com.example.movieticket.dto.response.BookingResponse;
import com.example.movieticket.model.Booking;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface BookingService {
    BookingResponse createBooking(BookingRequest request);
    BookingResponse handlePaymentReturn(HttpServletRequest request);
    void cancelExpiredBookings();
    BookingResponse getBookingById(Integer bookingId);
    List<BookingResponse> getUserBookings(Integer userId);
    Booking getBookingEntityById(Integer bookingId);
}

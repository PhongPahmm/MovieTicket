package com.example.movieticket.service;

import com.example.movieticket.dto.request.BookingRequest;
import com.example.movieticket.dto.response.BookingResponse;
import com.example.movieticket.dto.response.PageResponse;
import com.example.movieticket.model.Booking;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface BookingService {
    BookingResponse createBooking(BookingRequest request);
    BookingResponse handlePaymentReturn(HttpServletRequest request);
    BookingResponse getBookingById(Integer bookingId);
    List<BookingResponse> getUserBookings(Integer userId);
    Booking getBookingEntityById(Integer bookingId);
    PageResponse<BookingResponse> getAllBookings(int page, int size);
    BookingResponse cancelBooking(Integer bookingId);
}

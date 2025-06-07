package com.example.movieticket.controller;

import com.example.movieticket.dto.request.BookingRequest;
import com.example.movieticket.dto.response.BookingResponse;
import com.example.movieticket.dto.response.ResponseData;
import com.example.movieticket.service.BookingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingController {

    BookingService bookingService;

    @PostMapping("/book")
    public ResponseData<BookingResponse> createBooking(@RequestBody BookingRequest request) {
        BookingResponse response = bookingService.createBooking(request);
        return ResponseData.<BookingResponse>builder()
                .code(200)
                .message("Booking created successfully")
                .data(response)
                .build();
    }

    @GetMapping("/payment-return/vnpay-payment")
    public ResponseData<BookingResponse> handlePaymentReturn(HttpServletRequest request) {
        BookingResponse response = bookingService.handlePaymentReturn(request);
        return ResponseData.<BookingResponse>builder()
                .code(200)
                .message("Payment processed")
                .data(response)
                .build();
    }

    @GetMapping("/{bookingId}")
    public ResponseData<BookingResponse> getBookingById(@PathVariable Integer bookingId) {
        BookingResponse response = bookingService.getBookingById(bookingId);
        return ResponseData.<BookingResponse>builder()
                .code(200)
                .message("Booking fetched")
                .data(response)
                .build();
    }

    @GetMapping("/user/{userId}")
    public ResponseData<List<BookingResponse>> getUserBookings(@PathVariable Integer userId) {
        List<BookingResponse> responseList = bookingService.getUserBookings(userId);
        return ResponseData.<List<BookingResponse>>builder()
                .code(200)
                .message("User bookings fetched")
                .data(responseList)
                .build();
    }

}

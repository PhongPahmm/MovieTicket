package com.example.movieticket.controller;

import com.example.movieticket.dto.request.BookingRequest;
import com.example.movieticket.dto.response.BookingResponse;
import com.example.movieticket.dto.response.PageResponse;
import com.example.movieticket.dto.response.ResponseData;
import com.example.movieticket.service.BookingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingController {

    BookingService bookingService;

    @PostMapping("/book")
    public ResponseData<BookingResponse> createBooking(@RequestBody BookingRequest request, HttpServletRequest httpRequest) {
        BookingResponse response = bookingService.createBooking(request, httpRequest);
        return ResponseData.<BookingResponse>builder()
                .code(200)
                .message("Booking created successfully")
                .data(response)
                .build();
    }

    // IMPORTANT: Specific paths MUST come BEFORE generic path variables like /{bookingId}
    @GetMapping("/payment-return/vnpay-payment")
    public ResponseData<BookingResponse> handlePaymentReturn(HttpServletRequest request) {
        BookingResponse response = bookingService.handlePaymentReturn(request);
        return ResponseData.<BookingResponse>builder()
                .code(200)
                .message("Payment processed")
                .data(response)
                .build();
    }

    // Get current user's bookings (userId from query param)
    @GetMapping("/user")
    public ResponseData<PageResponse<BookingResponse>> getCurrentUserBookings(
            @RequestParam Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<BookingResponse> response = bookingService.getCurrentUserBookings(userId, page, size);
        return ResponseData.<PageResponse<BookingResponse>>builder()
                .code(200)
                .message("User bookings fetched")
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

    @GetMapping("")
    public ResponseData<PageResponse<BookingResponse>> getAllBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<BookingResponse> response = bookingService.getAllBookings(page, size);
        return ResponseData.<PageResponse<BookingResponse>>builder()
                .code(200)
                .message("All bookings fetched")
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

    @PutMapping("/cancel/{bookingId}")
    public ResponseData<BookingResponse> cancelBooking(@PathVariable Integer bookingId) {
        BookingResponse response = bookingService.cancelBooking(bookingId);
        return ResponseData.<BookingResponse>builder()
                .code(200)
                .message("Booking cancelled successfully")
                .data(response)
                .build();
    }

}

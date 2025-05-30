package com.example.movieticket.controller;

import com.example.movieticket.model.Booking;
import com.example.movieticket.service.BookingService;
import com.example.movieticket.service.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
@Slf4j
public class EmailController {

    private final EmailService emailService;
    private final BookingService bookingService;

    @PostMapping("/{bookingId}")
    public String sendTestEmail(@PathVariable Integer bookingId) {
        try {
            Booking booking = bookingService.getBookingEntityById(bookingId);
            emailService.sendBookingConfirmationEmail(booking);
            String email = booking.getUser() != null ? booking.getUser().getEmail() : "Email không xác định";
            return "Email sent successfully to: " + email;
        } catch (MessagingException e) {
            return "Failed to send email: " + e.getMessage();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

}

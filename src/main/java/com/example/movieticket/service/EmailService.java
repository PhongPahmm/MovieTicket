package com.example.movieticket.service;

import com.example.movieticket.model.Booking;
import jakarta.mail.MessagingException;

public interface EmailService {
    void sendBookingConfirmationEmail(Booking booking) throws MessagingException;
}

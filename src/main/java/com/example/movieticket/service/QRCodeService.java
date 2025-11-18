package com.example.movieticket.service;

import com.example.movieticket.model.Booking;

public interface QRCodeService {
    String generateQRCode(Booking booking);
    String buildQRContent(Booking booking);
}

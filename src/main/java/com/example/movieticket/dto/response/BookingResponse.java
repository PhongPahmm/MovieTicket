package com.example.movieticket.dto.response;

import com.example.movieticket.common.BookingStatus;
import com.example.movieticket.common.PaymentStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@Builder
public class BookingResponse {
    Integer bookingId;
    Integer userId;
    Integer showId;
    String movieTitle;
    String screenName;
    LocalDate showDate;
    LocalTime startTime;
    LocalDateTime bookingTime;
    LocalDateTime expireTime;
    BookingStatus status;
    Double totalAmount;
    Integer paymentId;
    PaymentStatus paymentStatus;
    String paymentUrl;
    String returnUrl;
    String qrCode; // Base64 encoded QR code image
    List<BookedSeatResponse> seats;
}

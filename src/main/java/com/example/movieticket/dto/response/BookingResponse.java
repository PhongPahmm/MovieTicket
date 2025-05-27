package com.example.movieticket.dto.response;

import com.example.movieticket.common.BookingStatus;
import com.example.movieticket.common.PaymentStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
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
    LocalDateTime bookingTime;
    LocalDateTime expireTime;
    BookingStatus status;
    Double totalAmount;
    Integer paymentId;
    PaymentStatus paymentStatus;
    String paymentUrl;
    String returnUrl;
    List<BookedSeatResponse> seats;
}

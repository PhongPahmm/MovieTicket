package com.example.movieticket.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BookingRequest {
    Integer showId;
    List<Integer> seats;
    PaymentRequest paymentRequest;
    String returnUrl;
}

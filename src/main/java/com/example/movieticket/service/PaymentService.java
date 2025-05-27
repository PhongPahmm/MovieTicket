package com.example.movieticket.service;

import com.example.movieticket.dto.request.PaymentRequest;
import com.example.movieticket.dto.response.PaymentResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface PaymentService {
    PaymentResponse createPayment(PaymentRequest request, HttpServletRequest httpServletRequest);
}

package com.example.movieticket.service.impl;

import com.example.movieticket.dto.request.PaymentRequest;
import com.example.movieticket.dto.response.PaymentResponse;
import com.example.movieticket.repository.PaymentRepository;
import com.example.movieticket.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentServiceImpl implements PaymentService {
    PaymentRepository paymentRepository;

    @Override
    public PaymentResponse createPayment(PaymentRequest request, HttpServletRequest httpServletRequest) {
        return null;
    }
}

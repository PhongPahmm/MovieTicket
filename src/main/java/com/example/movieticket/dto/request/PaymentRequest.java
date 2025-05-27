package com.example.movieticket.dto.request;

import com.example.movieticket.common.PaymentMethod;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {
    private PaymentMethod paymentMethod;
    private String orderInfo;
    private Double amount;
}

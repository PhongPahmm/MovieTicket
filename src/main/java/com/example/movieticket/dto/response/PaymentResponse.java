package com.example.movieticket.dto.response;

import com.example.movieticket.common.PaymentMethod;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PaymentResponse {
    private Integer paymentId;
    private PaymentMethod paymentMethod;
    private String orderInfo;
    private Double amount;
}

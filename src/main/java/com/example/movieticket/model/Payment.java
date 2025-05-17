package com.example.movieticket.model;

import com.example.movieticket.common.PaymentMethod;
import com.example.movieticket.common.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "payments")
public class Payment extends AbstractEntity<Integer> {
    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;
    int amount;

    @Enumerated(EnumType.STRING)
    PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    PaymentStatus status;

}

package com.example.movieticket.repository;

import com.example.movieticket.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    Optional<Payment> findByOrderInfo(String orderInfo);
}

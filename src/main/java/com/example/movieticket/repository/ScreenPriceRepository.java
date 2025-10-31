package com.example.movieticket.repository;

import com.example.movieticket.common.SeatType;
import com.example.movieticket.model.ScreenPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScreenPriceRepository extends JpaRepository<ScreenPrice, Integer> {
    Optional<ScreenPrice> findByScreen_IdAndSeatType(Integer screenId, SeatType seatType);
}



package com.example.movieticket.repository;

import com.example.movieticket.common.SeatType;
import com.example.movieticket.model.Price;
import com.example.movieticket.model.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceRepository extends JpaRepository<Price, Integer> {
    List<Price> findByShow(Show show);

    Optional<Price> findFirstByShowIdAndSeatTypeAndValidFromLessThanEqualAndValidToGreaterThanEqual(
            Integer showId,
            SeatType seatType,
            LocalDate validFrom,
            LocalDate validTo
    );
}

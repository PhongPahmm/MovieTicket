package com.example.movieticket.repository;

import com.example.movieticket.common.SeatType;
import com.example.movieticket.model.Price;
import com.example.movieticket.model.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT p " +
            "FROM Price p " +
            "WHERE p.show = :show " +
            "AND p.seatType = :seatType " +
            "AND :date BETWEEN p.validFrom " +
            "AND p.validTo")
    Optional<Price> findByShowAndSeatTypeAndDateBetween(
            @Param("show") Show show,
            @Param("seatType") SeatType seatType,
            @Param("date") LocalDate date);

    // Check if price exists with overlapping date range
    @Query("""
        SELECT COUNT(p) > 0 FROM Price p
        WHERE p.show = :show
        AND p.seatType = :seatType
        AND (
            (:validFrom BETWEEN p.validFrom AND p.validTo)
            OR (:validTo BETWEEN p.validFrom AND p.validTo)
            OR (p.validFrom BETWEEN :validFrom AND :validTo)
        )
        AND (:excludePriceId IS NULL OR p.id != :excludePriceId)
    """)
    boolean existsByShowAndSeatTypeWithOverlappingDateRange(
            @Param("show") Show show,
            @Param("seatType") SeatType seatType,
            @Param("validFrom") LocalDate validFrom,
            @Param("validTo") LocalDate validTo,
            @Param("excludePriceId") Integer excludePriceId
    );
}

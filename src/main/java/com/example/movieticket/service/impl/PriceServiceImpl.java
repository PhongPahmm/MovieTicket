package com.example.movieticket.service.impl;

import com.example.movieticket.common.SeatType;
import com.example.movieticket.dto.request.PriceRequest;
import com.example.movieticket.dto.response.PageResponse;
import com.example.movieticket.dto.response.PriceResponse;
import com.example.movieticket.exception.AppException;
import com.example.movieticket.exception.ErrorCode;
import com.example.movieticket.model.Price;
import com.example.movieticket.model.Seat;
import com.example.movieticket.model.Show;
import com.example.movieticket.repository.PriceRepository;
import com.example.movieticket.repository.SeatRepository;
import com.example.movieticket.repository.ShowRepository;
import com.example.movieticket.service.PriceService;
import com.example.movieticket.util.PaginationUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PriceServiceImpl implements PriceService {
    PriceRepository priceRepository;
    ShowRepository showRepository;
    SeatRepository seatRepository;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public PriceResponse createPrice(PriceRequest priceRequest) {
        Show show = showRepository.findById(priceRequest.getShowId())
                .orElseThrow(() -> new AppException(ErrorCode.SHOW_NOT_FOUND));
        Price price = Price.builder()
                .show(show)
                .amount(priceRequest.getAmount())
                .seatType(priceRequest.getSeatType())
                .validFrom(priceRequest.getValidFrom())
                .validTo(priceRequest.getValidTo())
                .build();
        return mapToPriceResponse(priceRepository.save(price));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public PriceResponse updatePrice(Integer priceId, PriceRequest request) {
        Price price = priceRepository.findById(priceId)
                .orElseThrow(() -> new AppException(ErrorCode.PRICE_NOT_FOUND));
        if (request.getAmount() != null) price.setAmount(request.getAmount());
        if (request.getSeatType() != null) price.setSeatType(request.getSeatType());
        if (request.getValidFrom() != null) price.setValidFrom(request.getValidFrom());
        if (request.getValidTo() != null) price.setValidTo(request.getValidTo());
        return mapToPriceResponse(priceRepository.save(price));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Void deletePrice(Integer priceId) {
        Price price = priceRepository.findById(priceId)
                .orElseThrow(() -> new AppException(ErrorCode.PRICE_NOT_FOUND));
        priceRepository.delete(price);
        return null;
    }

    @Override
    public List<PriceResponse> getPricesByShow(Integer showId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new AppException(ErrorCode.SHOW_NOT_FOUND));
        return priceRepository.findByShow(show)
                .stream().map(this::mapToPriceResponse).toList();
    }

    @Override
    public PriceResponse getValidPrice(Integer showId, SeatType seatType, LocalDate date) {
        Price price = priceRepository
                .findFirstByShowIdAndSeatTypeAndValidFromLessThanEqualAndValidToGreaterThanEqual(
                        showId, seatType, date, date)
                .orElseThrow(() -> new AppException(ErrorCode.PRICE_NOT_FOUND));
        return mapToPriceResponse(price);
    }

    @Override
    public PageResponse<PriceResponse> getAllPrices(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Price> prices = priceRepository.findAll(pageable);
        return PaginationUtil.mapToPageResponse(prices, this::mapToPriceResponse);
    }
    @Override
    public PriceResponse getPriceByShowAndSeat(Integer showId, Integer seatId) {
        Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new AppException(ErrorCode.SEAT_NOT_FOUND));

        SeatType seatType = seat.getSeatType();

        LocalDate today = LocalDate.now();

        Price price = priceRepository
                .findFirstByShowIdAndSeatTypeAndValidFromLessThanEqualAndValidToGreaterThanEqual(
                        showId, seatType, today, today)
                .orElseThrow(() -> new AppException(ErrorCode.PRICE_NOT_FOUND));

        return mapToPriceResponse(price);
    }

    private PriceResponse mapToPriceResponse(Price price) {
        return PriceResponse.builder()
                .priceId(price.getId())
                .showId(price.getShow().getId())
                .seatType(price.getSeatType())
                .amount(price.getAmount())
                .validFrom(price.getValidFrom())
                .validTo(price.getValidTo())
                .createdAt(price.getCreatedAt())
                .updatedAt(price.getUpdatedAt())
                .build();
    }
}

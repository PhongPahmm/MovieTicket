package com.example.movieticket.service.impl;

import com.example.movieticket.common.SeatType;
import com.example.movieticket.dto.request.PriceRequest;
import com.example.movieticket.dto.response.PageResponse;
import com.example.movieticket.dto.response.PriceResponse;
import com.example.movieticket.dto.response.ScreenPriceResponse;
import com.example.movieticket.exception.AppException;
import com.example.movieticket.exception.ErrorCode;
import com.example.movieticket.model.Price;
import com.example.movieticket.model.Seat;
import com.example.movieticket.model.ScreenPrice;
import com.example.movieticket.model.Show;
import com.example.movieticket.repository.PriceRepository;
import com.example.movieticket.repository.ScreenPriceRepository;
import com.example.movieticket.repository.ScreenRepository;
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
    ScreenPriceRepository screenPriceRepository;
    ScreenRepository screenRepository;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public PriceResponse createPrice(PriceRequest priceRequest) {
        // Validate show exists
        Show show = showRepository.findById(priceRequest.getShowId())
                .orElseThrow(() -> new AppException(ErrorCode.SHOW_NOT_FOUND));

        // Validate amount
        if (priceRequest.getAmount() == null || priceRequest.getAmount() <= 0) {
            throw new AppException(ErrorCode.INVALID_PRICE_AMOUNT);
        }

        // Validate date range
        if (priceRequest.getValidFrom() == null || priceRequest.getValidTo() == null) {
            throw new AppException(ErrorCode.INVALID_PRICE_DATE_RANGE);
        }

        if (priceRequest.getValidFrom().isAfter(priceRequest.getValidTo())) {
            throw new AppException(ErrorCode.INVALID_PRICE_DATE_RANGE);
        }

        // Check for overlapping price entries
        boolean hasOverlap = priceRepository.existsByShowAndSeatTypeWithOverlappingDateRange(
                show,
                priceRequest.getSeatType(),
                priceRequest.getValidFrom(),
                priceRequest.getValidTo(),
                null // excludePriceId is null for create
        );

        if (hasOverlap) {
            throw new AppException(ErrorCode.PRICE_ALREADY_EXISTS);
        }

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

        // Validate amount if provided
        if (request.getAmount() != null && request.getAmount() <= 0) {
            throw new AppException(ErrorCode.INVALID_PRICE_AMOUNT);
        }

        // Update fields
        if (request.getAmount() != null) price.setAmount(request.getAmount());
        if (request.getSeatType() != null) price.setSeatType(request.getSeatType());
        if (request.getValidFrom() != null) price.setValidFrom(request.getValidFrom());
        if (request.getValidTo() != null) price.setValidTo(request.getValidTo());

        // Validate date range after updates
        if (price.getValidFrom().isAfter(price.getValidTo())) {
            throw new AppException(ErrorCode.INVALID_PRICE_DATE_RANGE);
        }

        // Check for overlapping price entries (excluding current price)
        boolean hasOverlap = priceRepository.existsByShowAndSeatTypeWithOverlappingDateRange(
                price.getShow(),
                price.getSeatType(),
                price.getValidFrom(),
                price.getValidTo(),
                priceId // exclude current price from check
        );

        if (hasOverlap) {
            throw new AppException(ErrorCode.PRICE_ALREADY_EXISTS);
        }

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
        // Try show-specific price first
        var priceOpt = priceRepository
                .findFirstByShowIdAndSeatTypeAndValidFromLessThanEqualAndValidToGreaterThanEqual(
                        showId, seatType, date, date);
        if (priceOpt.isPresent()) {
            return mapToPriceResponse(priceOpt.get());
        }
        // Fallback to screen default price
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new AppException(ErrorCode.SHOW_NOT_FOUND));
        Integer amount = getAmountByScreenAndSeat(show.getScreen().getId(), seatType);
        if (amount == null || amount <= 0) {
            throw new AppException(ErrorCode.PRICE_NOT_FOUND);
        }
        return PriceResponse.builder()
                .priceId(null)
                .showId(showId)
                .movieTitle(show.getMovie().getTitle())
                .seatType(seatType)
                .amount(amount)
                .validFrom(date)
                .validTo(date)
                .build();
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

        var priceOpt = priceRepository
                .findFirstByShowIdAndSeatTypeAndValidFromLessThanEqualAndValidToGreaterThanEqual(
                        showId, seatType, today, today);
        if (priceOpt.isPresent()) {
            return mapToPriceResponse(priceOpt.get());
        }
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new AppException(ErrorCode.SHOW_NOT_FOUND));
        Integer amount = getAmountByScreenAndSeat(show.getScreen().getId(), seatType);
        if (amount == null || amount <= 0) {
            throw new AppException(ErrorCode.PRICE_NOT_FOUND);
        }
        return PriceResponse.builder()
                .priceId(null)
                .showId(showId)
                .movieTitle(show.getMovie().getTitle())
                .seatType(seatType)
                .amount(amount)
                .validFrom(today)
                .validTo(today)
                .build();
    }

    @Override
    public Integer getAmountByScreenAndSeat(Integer screenId, SeatType seatType) {
        return screenPriceRepository
                .findByScreen_IdAndSeatType(screenId, seatType)
                .map(sp -> sp.getAmount())
                .orElse(0);
    }

    @Override
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ScreenPriceResponse createOrUpdateScreenPrice(com.example.movieticket.dto.request.ScreenPriceRequest request) {
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new AppException(ErrorCode.INVALID_PRICE_AMOUNT);
        }
        var screen = screenRepository.findById(request.getScreenId())
                .orElseThrow(() -> new AppException(ErrorCode.SCREEN_NOT_FOUND));

        var existing = screenPriceRepository.findByScreen_IdAndSeatType(request.getScreenId(), request.getSeatType());
        ScreenPrice entity = existing.orElseGet(() -> ScreenPrice.builder()
                .screen(screen)
                .seatType(request.getSeatType())
                .build());
        entity.setAmount(request.getAmount());
        var saved = screenPriceRepository.save(entity);
        return ScreenPriceResponse.builder()
                .id(saved.getId())
                .screenId(saved.getScreen().getId())
                .seatType(saved.getSeatType())
                .amount(saved.getAmount())
                .build();
    }

    @Override
    public java.util.List<ScreenPriceResponse> getScreenPricesByScreen(Integer screenId) {
        return screenPriceRepository.findAll().stream()
                .filter(sp -> sp.getScreen().getId().equals(screenId))
                .map(sp -> ScreenPriceResponse.builder()
                        .id(sp.getId())
                        .screenId(sp.getScreen().getId())
                        .seatType(sp.getSeatType())
                        .amount(sp.getAmount())
                        .build())
                .toList();
    }

    private PriceResponse mapToPriceResponse(Price price) {
        return PriceResponse.builder()
                .priceId(price.getId())
                .showId(price.getShow().getId())
                .movieTitle(price.getShow().getMovie().getTitle())
                .seatType(price.getSeatType())
                .amount(price.getAmount())
                .validFrom(price.getValidFrom())
                .validTo(price.getValidTo())
                .createdAt(price.getCreatedAt())
                .updatedAt(price.getUpdatedAt())
                .build();
    }
}

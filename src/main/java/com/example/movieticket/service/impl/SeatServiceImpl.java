package com.example.movieticket.service.impl;

import com.example.movieticket.dto.request.SeatRequest;
import com.example.movieticket.dto.response.SeatResponse;
import com.example.movieticket.exception.AppException;
import com.example.movieticket.exception.ErrorCode;
import com.example.movieticket.model.Seat;
import com.example.movieticket.repository.BookingSeatRepository;
import com.example.movieticket.repository.ScreenRepository;
import com.example.movieticket.repository.SeatRepository;
import com.example.movieticket.repository.ShowRepository;
import com.example.movieticket.service.SeatService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SeatServiceImpl implements SeatService {
    ScreenRepository screenRepository;
    SeatRepository seatRepository;
    ShowRepository showRepository;
    BookingSeatRepository bookingSeatRepository;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public SeatResponse createSeat(SeatRequest request) {
        var screen = screenRepository.findById(request.getScreenId())
                .orElseThrow(() -> new AppException(ErrorCode.SCREEN_NOT_FOUND));

        Seat seat = Seat.builder()
                .screen(screen)
                .seatRow(request.getRow())
                .number(request.getNumber())
                .seatType(request.getSeatType())
                .active(true)
                .build();
        return mapToResponse(seatRepository.save(seat));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public SeatResponse updateSeat(Integer seatId, SeatRequest request) {
        var seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new AppException(ErrorCode.SEAT_NOT_FOUND));

        if(request.getRow() != null) seat.setSeatRow(request.getRow());
        if(request.getNumber() != null) seat.setNumber(request.getNumber());
        if(request.getSeatType() != null) seat.setSeatType(request.getSeatType());
        if(request.getActive() != null) seat.setActive(request.getActive());

        return mapToResponse(seatRepository.save(seat));
    }

    @Override
    public List<SeatResponse> getSeatsByScreen(Integer screenId) {
        return seatRepository.findByScreenId(screenId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<SeatResponse> getAvailableSeatsByShow(Integer showId) {
        var show = showRepository.findById(showId)
                .orElseThrow(() -> new AppException(ErrorCode.SHOW_NOT_FOUND));
        var allSeats = seatRepository.findByScreenId(show.getScreen().getId());
        var bookedSeatIds = bookingSeatRepository.findByBookingShow_Id(showId).stream()
                .map(bs -> bs.getSeat().getId())
                .toList();

        return allSeats.stream()
                .filter(seat -> !bookedSeatIds.contains(seat.getId()))
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Void deleteSeat(Integer seatId) {
        seatRepository.deleteById(seatId);
        return null;
    }

    private SeatResponse mapToResponse(Seat seat) {
        return SeatResponse.builder()
                .id(seat.getId())
                .screenId(seat.getScreen().getId())
                .row(seat.getSeatRow())
                .number(seat.getNumber())
                .seatType(seat.getSeatType())
                .active(seat.isActive())
                .build();
    }
}

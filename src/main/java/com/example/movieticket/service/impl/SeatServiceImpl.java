package com.example.movieticket.service.impl;

import com.example.movieticket.common.SeatStatus;
import com.example.movieticket.dto.request.SeatRequest;
import com.example.movieticket.dto.response.SeatResponse;
import com.example.movieticket.exception.AppException;
import com.example.movieticket.exception.ErrorCode;
import com.example.movieticket.model.BookingSeat;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public List<SeatResponse> getSeatsByShow(Integer showId) {
        var show = showRepository.findById(showId)
                .orElseThrow(() -> new AppException(ErrorCode.SHOW_NOT_FOUND));

        List<Seat> allSeats = seatRepository.findByScreenId(show.getScreen().getId());

        List<BookingSeat> bookingSeats = bookingSeatRepository.findByBooking_Show_Id(showId);

        Map<Integer, SeatStatus> seatStatusMap = new HashMap<>();
        for (BookingSeat bs : bookingSeats) {
            int seatId = bs.getSeat().getId();
            SeatStatus currentStatus = seatStatusMap.get(seatId);
            SeatStatus newStatus = bs.getStatus();

            if (currentStatus == null) {
                seatStatusMap.put(seatId, newStatus);
            } else {
                if (newStatus == SeatStatus.BOOKED) {
                    seatStatusMap.put(seatId, SeatStatus.BOOKED);
                }
                else if (currentStatus == SeatStatus.AVAILABLE && newStatus == SeatStatus.PENDING) {
                    seatStatusMap.put(seatId, SeatStatus.PENDING);
                }
            }
        }

        return allSeats.stream()
                .map(seat -> {
                    SeatStatus status = seatStatusMap.getOrDefault(seat.getId(), SeatStatus.AVAILABLE);
                    return mapToResponse(seat, status);
                })
                .toList();
    }




    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Void deleteSeat(Integer seatId) {
        seatRepository.deleteById(seatId);
        return null;
    }

    private SeatResponse mapToResponse(Seat seat, SeatStatus status) {
        return SeatResponse.builder()
                .id(seat.getId())
                .screenId(seat.getScreen().getId())
                .row(seat.getSeatRow())
                .number(seat.getNumber())
                .seatType(seat.getSeatType())
                .active(seat.isActive())
                .status(status)
                .build();
    }
    private SeatResponse mapToResponse(Seat seat) {
        return mapToResponse(seat, SeatStatus.AVAILABLE);
    }
}

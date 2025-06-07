package com.example.movieticket.controller;

import com.example.movieticket.dto.request.SeatRequest;
import com.example.movieticket.dto.response.ResponseData;
import com.example.movieticket.dto.response.SeatResponse;
import com.example.movieticket.service.SeatService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/seats")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SeatController {

    SeatService seatService;

    @GetMapping("/screen/{screenId}")
    public ResponseEntity<ResponseData<List<SeatResponse>>> getSeatsByScreen(@PathVariable Integer screenId) {
        List<SeatResponse> seats = seatService.getSeatsByScreen(screenId);
        return ResponseEntity.ok(
                ResponseData.<List<SeatResponse>>builder()
                        .code(200)
                        .message("Fetched seats by screen")
                        .data(seats)
                        .build()
        );
    }

    @GetMapping("/show/{showId}")
    public ResponseEntity<ResponseData<List<SeatResponse>>> getAvailableSeatByShow(@PathVariable Integer showId) {
        List<SeatResponse> seats = seatService.getSeatsByShow(showId);
        return ResponseEntity.ok(
                ResponseData.<List<SeatResponse>>builder()
                        .code(200)
                        .message("Fetched seats by show")
                        .data(seats)
                        .build()
        );
    }

    @PostMapping("/add-seat")
    public ResponseEntity<ResponseData<SeatResponse>> createSeat(@RequestBody SeatRequest request) {
        SeatResponse seat = seatService.createSeat(request);
        return ResponseEntity.ok(
                ResponseData.<SeatResponse>builder()
                        .code(200)
                        .message("Seat created")
                        .data(seat)
                        .build()
        );
    }

    @PutMapping("/update-seat/{id}")
    public ResponseEntity<ResponseData<SeatResponse>> updateSeat(@PathVariable Integer id,
                                                                 @RequestBody SeatRequest request) {
        SeatResponse seat = seatService.updateSeat(id, request);
        return ResponseEntity.ok(
                ResponseData.<SeatResponse>builder()
                        .code(200)
                        .message("Seat updated")
                        .data(seat)
                        .build()
        );
    }

    @DeleteMapping("/{id}")

    public ResponseEntity<ResponseData<Void>> deleteSeat(@PathVariable Integer id) {
        seatService.deleteSeat(id);
        return ResponseEntity.ok(
                ResponseData.<Void>builder()
                        .code(200)
                        .message("Seat deleted")
                        .build()
        );
    }
}

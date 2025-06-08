package com.example.movieticket.controller;

import com.example.movieticket.common.SeatType;
import com.example.movieticket.dto.request.PriceRequest;
import com.example.movieticket.dto.response.PageResponse;
import com.example.movieticket.dto.response.PriceResponse;
import com.example.movieticket.dto.response.ResponseData;
import com.example.movieticket.service.PriceService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/prices")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PriceController {

    PriceService priceService;

    @PostMapping("/add-price")
    public ResponseData<PriceResponse> createPrice(@RequestBody PriceRequest request) {
        var result = priceService.createPrice(request);
        return ResponseData.<PriceResponse>builder()
                .code(200)
                .message("Create price successfully")
                .data(result)
                .build();
    }

    @PutMapping("/update-price/{priceId}")
    public ResponseData<PriceResponse> updatePrice(@PathVariable Integer priceId, @RequestBody PriceRequest request) {
        var result = priceService.updatePrice(priceId, request);
        return ResponseData.<PriceResponse>builder()
                .code(200)
                .message("Update price successfully")
                .data(result)
                .build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseData<Void> deletePrice(@PathVariable Integer id) {
        priceService.deletePrice(id);
        return ResponseData.<Void>builder()
                .code(200)
                .message("Delete price successfully")
                .build();
    }

    @GetMapping("/show/{showId}")
    public ResponseData<List<PriceResponse>> getPricesByShow(@PathVariable Integer showId) {
        var result = priceService.getPricesByShow(showId);
        return ResponseData.<List<PriceResponse>>builder()
                .code(200)
                .message("Get prices by show successfully")
                .data(result)
                .build();
    }

    @GetMapping("/valid-price")
    public ResponseData<PriceResponse> getValidPrice(
            @RequestParam Integer showId,
            @RequestParam SeatType seatType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        var result = priceService.getValidPrice(showId, seatType, date);
        return ResponseData.<PriceResponse>builder()
                .code(200)
                .message("Get valid price successfully")
                .data(result)
                .build();
    }
    @GetMapping("/by-show-seat")
    public ResponseData<PriceResponse> getPriceByShowAndSeat(
            @RequestParam Integer showId,
            @RequestParam Integer seatId) {
        var result = priceService.getPriceByShowAndSeat(showId, seatId);
        return ResponseData.<PriceResponse>builder()
                .code(200)
                .message("Get price by show and seat successfully")
                .data(result)
                .build();
    }

    @GetMapping("")
    public ResponseData<PageResponse<PriceResponse>> getAllPrices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var result = priceService.getAllPrices(page, size);
        return ResponseData.<PageResponse<PriceResponse>>builder()
                .code(200)
                .message("Get all prices successfully")
                .data(result)
                .build();
    }
}


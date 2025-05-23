package com.example.movieticket.controller;

import com.example.movieticket.dto.request.ScreenRequest;
import com.example.movieticket.dto.response.ResponseData;
import com.example.movieticket.dto.response.ScreenResponse;
import com.example.movieticket.service.ScreenService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/screens")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ScreenController {
    ScreenService screenService;

    @PostMapping("/add-screen")
    public ResponseData<ScreenResponse> addScreen(@RequestBody ScreenRequest request) {
        return ResponseData.<ScreenResponse>builder()
                .code(200)
                .message("Successfully added screen")
                .data(screenService.createScreen(request))
                .build();
    }

    @PutMapping("update-screen/{screenId}")
    public ResponseData<ScreenResponse> updateScreen(@PathVariable Integer screenId, @RequestBody ScreenRequest request) {
        return ResponseData.<ScreenResponse>builder()
                .code(200)
                .message("Successfully updated screen")
                .data(screenService.updateScreen(screenId, request))
                .build();
    }

    @GetMapping("")
    public ResponseData<List<ScreenResponse>> getAllScreens() {
        return ResponseData.<List<ScreenResponse>>builder()
                .code(200)
                .message("Successfully retrieved all screens")
                .data(screenService.getAllScreens())
                .build();
    }

    @GetMapping("/{screenId}")
    public ResponseData<ScreenResponse> getScreen(@PathVariable Integer screenId) {
        return ResponseData.<ScreenResponse>builder()
                .code(200)
                .message("Successfully retrieved screen")
                .data(screenService.getScreeById(screenId))
                .build();
    }

    @PutMapping("/status/{screenId}")
    public ResponseData<ScreenResponse> changeScreenStatus(@PathVariable Integer screenId,
                                                           @RequestParam boolean isActive) {
        return ResponseData.<ScreenResponse>builder()
                .code(200)
                .message("Successfully changed screen status")
                .data(screenService.changeScreenStatus(screenId, isActive))
                .build();
    }
}

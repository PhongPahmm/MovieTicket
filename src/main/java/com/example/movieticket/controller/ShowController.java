package com.example.movieticket.controller;

import com.example.movieticket.dto.request.ShowRequest;
import com.example.movieticket.dto.response.PageResponse;
import com.example.movieticket.dto.response.ResponseData;
import com.example.movieticket.dto.response.ShowResponse;
import com.example.movieticket.service.ShowService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shows")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShowController {
    ShowService showService;

    @PostMapping("/add-show")
    public ResponseData<ShowResponse> addShow(@RequestBody ShowRequest request){
        return ResponseData.<ShowResponse>builder()
                .code(200)
                .message("Successfully added show")
                .data(showService.createShow(request))
                .build();
    }

    @PutMapping("/update-show/{showId}")
    public ResponseData<ShowResponse> updateShow(@PathVariable Integer showId, @RequestBody ShowRequest request){
        return ResponseData.<ShowResponse>builder()
                .code(200)
                .message("Successfully updated show")
                .data(showService.updateShow(showId, request))
                .build();
    }

    @GetMapping("")
    public ResponseData<PageResponse<ShowResponse>> getAllShow(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseData.<PageResponse<ShowResponse>>builder()
                .code(200)
                .message("Successfully retrieved shows")
                .data(showService.getAllShow(page, size))
                .build();
    }
    @GetMapping("/movie/{movieId}")
    public ResponseData<PageResponse<ShowResponse>> getShowByMovie(
            @PathVariable Integer movieId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseData.<PageResponse<ShowResponse>>builder()
                .code(200)
                .message("Successfully retrieved shows by movie")
                .data(showService.getShowByMovie(movieId, page, size))
                .build();
    }
    @PutMapping("/status/{showId}")
    public ResponseData<ShowResponse> changeShowActive(@PathVariable Integer showId, @RequestParam boolean isActive){
        return ResponseData.<ShowResponse>builder()
                .code(200)
                .message("Successfully changed show")
                .data(showService.changeActive(showId, isActive))
                .build();
    }
}
